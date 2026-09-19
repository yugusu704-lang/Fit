package com.fit.tracker.domain.engine

import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.domain.model.ExerciseDefinition
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class CalorieConservationEngine {

    /**
     * Initial allocation when user selects N exercises.
     * Evenly splits the target calories and quantizes to discrete physical units.
     * Uses a designated Slack Absorber to capture quantization residuals within 1 kcal.
     */
    fun allocateInitial(
        exercises: List<ExerciseDefinition>,
        targetTotalKcal: Int
    ): List<ExerciseAllocation> {
        if (exercises.isEmpty() || targetTotalKcal <= 0) {
            return exercises.map { ExerciseAllocation(it, 0, 0) }
        }

        val n = exercises.size
        val targetPerItem = targetTotalKcal.toDouble() / n

        // First pass: Quantize each to nearest stepQuantum
        val candidateAllocations = exercises.map { exercise ->
            val rawUnits = targetPerItem / exercise.kcalPerUnit
            val stepCount = (rawUnits / exercise.stepQuantum).roundToInt()
            val quantizedUnits = max(exercise.minUnits, stepCount * exercise.stepQuantum)
            val kcal = (quantizedUnits * exercise.kcalPerUnit).roundToInt()
            ExerciseAllocation(exercise, quantizedUnits, kcal)
        }.toMutableList()

        // Slack Absorption: Designate exercise with smallest quantum to absorb residual
        absorbQuantizationResidual(candidateAllocations, targetTotalKcal)

        return candidateAllocations
    }

    /**
     * Rebalance allocations when user adjusts one exercise's calories/units.
     * Guardrail 1: Discrete unit quantization with Slack Absorber.
     * Guardrail 2: Completed exercises are immutable.
     * Guardrail 3: Locked exercises remain unchanged.
     * Guardrail 4: Floor clamped at 0.
     */
    fun rebalance(
        allocations: List<ExerciseAllocation>,
        targetTotalKcal: Int,
        modifiedExerciseId: String,
        requestedCalories: Int,
        allowOverflow: Boolean = false
    ): List<ExerciseAllocation> {
        val targetIndex = allocations.indexOfFirst { it.exercise.id == modifiedExerciseId }
        if (targetIndex == -1) return allocations

        val targetItem = allocations[targetIndex]
        // Guardrail: Completed or locked items cannot be directly modified
        if (targetItem.isCompleted || targetItem.isLocked) return allocations

        // Partition into fixed vs adjustable
        val fixedCalories = allocations
            .filter { it.exercise.id != modifiedExerciseId && (it.isCompleted || it.isLocked) }
            .sumOf { it.calories }

        val adjustableOthers = allocations.filter {
            it.exercise.id != modifiedExerciseId && !it.isCompleted && !it.isLocked
        }

        val availableForModifiedAndOthers = max(0, targetTotalKcal - fixedCalories)

        if (adjustableOthers.isEmpty()) {
            // No other item can adjust: clamp modified item directly unless overflow allowed
            val targetKcal = if (allowOverflow) max(0, requestedCalories) else requestedCalories.coerceIn(0, availableForModifiedAndOthers)
            val quantizedUnits = quantizeUnits(targetItem.exercise, targetKcal)
            val finalKcal = (quantizedUnits * targetItem.exercise.kcalPerUnit).roundToInt()
            return allocations.map {
                if (it.exercise.id == modifiedExerciseId) it.copy(units = quantizedUnits, calories = finalKcal)
                else it
            }
        }

        // When adjustable others exist
        val isOverflow = allowOverflow && (requestedCalories > availableForModifiedAndOthers)
        val clampedModifiedKcal = if (isOverflow) {
            max(0, requestedCalories)
        } else {
            requestedCalories.coerceIn(0, availableForModifiedAndOthers)
        }
        val remainingForOthers = if (isOverflow) 0 else availableForModifiedAndOthers - clampedModifiedKcal

        // Distribute remaining among adjustable others
        val result = allocations.toMutableList()

        if (adjustableOthers.size == 1) {
            val other = adjustableOthers.first()
            val otherUnits = quantizeUnits(other.exercise, remainingForOthers)
            val otherKcal = (otherUnits * other.exercise.kcalPerUnit).roundToInt()
            val otherIndex = result.indexOfFirst { it.exercise.id == other.exercise.id }
            result[otherIndex] = other.copy(units = otherUnits, calories = otherKcal)

            val modifiedUnits = quantizeUnits(targetItem.exercise, clampedModifiedKcal)
            val modifiedKcal = (modifiedUnits * targetItem.exercise.kcalPerUnit).roundToInt()
            result[targetIndex] = targetItem.copy(units = modifiedUnits, calories = modifiedKcal)
        } else {
            // 2 or more adjustable others: proportional distribution
            val sumOtherCurrent = adjustableOthers.sumOf { it.calories }
            val otherTargetAllocations = mutableMapOf<String, Int>()

            if (sumOtherCurrent > 0) {
                var allocated = 0
                adjustableOthers.forEachIndexed { index, other ->
                    if (index == adjustableOthers.lastIndex) {
                        otherTargetAllocations[other.exercise.id] = max(0, remainingForOthers - allocated)
                    } else {
                        val share = ((other.calories.toDouble() / sumOtherCurrent) * remainingForOthers).roundToInt()
                        otherTargetAllocations[other.exercise.id] = share
                        allocated += share
                    }
                }
            } else {
                // Even distribution if all were 0
                val even = remainingForOthers / adjustableOthers.size
                adjustableOthers.forEach { otherTargetAllocations[it.exercise.id] = even }
            }

            // Apply to results
            adjustableOthers.forEach { other ->
                val desiredKcal = otherTargetAllocations[other.exercise.id] ?: 0
                val qUnits = quantizeUnits(other.exercise, desiredKcal)
                val qKcal = (qUnits * other.exercise.kcalPerUnit).roundToInt()
                val idx = result.indexOfFirst { it.exercise.id == other.exercise.id }
                result[idx] = other.copy(units = qUnits, calories = qKcal)
            }

            val modUnits = quantizeUnits(targetItem.exercise, clampedModifiedKcal)
            val modKcal = (modUnits * targetItem.exercise.kcalPerUnit).roundToInt()
            result[targetIndex] = targetItem.copy(units = modUnits, calories = modKcal)
        }

        // Final slack absorption across uncompleted & unlocked items
        if (!isOverflow) {
            absorbQuantizationResidual(result, targetTotalKcal, excludeExerciseId = modifiedExerciseId)
        }

        return result
    }

    /**
     * Rebalance allocations when the global target calories change (e.g. after profile update or food added).
     * Guardrail:
     * - Completed exercises and locked exercises are strictly preserved (immutable).
     * - Remaining uncompleted, unlocked exercises absorb the difference.
     * - Residual quantization is absorbed by the designated slack absorber.
     */
    fun rebalanceForNewTarget(
        allocations: List<ExerciseAllocation>,
        newTargetTotalKcal: Int
    ): List<ExerciseAllocation> {
        if (allocations.isEmpty()) return allocations

        val fixedCalories = allocations.filter { it.isCompleted || it.isLocked }.sumOf { it.calories }
        val remainingTarget = max(0, newTargetTotalKcal - fixedCalories)
        val adjustable = allocations.filter { !it.isCompleted && !it.isLocked }

        if (adjustable.isEmpty()) {
            return allocations
        }

        val newlyAllocated = allocateInitial(adjustable.map { it.exercise }, remainingTarget)
        val newlyAllocatedMap = newlyAllocated.associateBy { it.exercise.id }

        val result = allocations.map { alloc ->
            if (alloc.isCompleted || alloc.isLocked) {
                alloc
            } else {
                newlyAllocatedMap[alloc.exercise.id]?.copy(
                    isCompleted = false,
                    isLocked = false
                ) ?: alloc
            }
        }.toMutableList()

        absorbQuantizationResidual(result, newTargetTotalKcal)

        return result
    }

    private fun quantizeUnits(exercise: ExerciseDefinition, desiredKcal: Int): Int {
        if (desiredKcal <= 0) return 0
        val rawUnits = desiredKcal.toDouble() / exercise.kcalPerUnit
        val steps = (rawUnits / exercise.stepQuantum).roundToInt()
        return max(exercise.minUnits, steps * exercise.stepQuantum)
    }

    private fun absorbQuantizationResidual(
        allocations: MutableList<ExerciseAllocation>,
        targetTotalKcal: Int,
        excludeExerciseId: String? = null
    ) {
        val currentSum = allocations.sumOf { it.calories }
        val residual = targetTotalKcal - currentSum
        if (abs(residual) <= 1) return

        // 1. Try candidates other than excludeExerciseId first to preserve modified item's exact units
        if (excludeExerciseId != null) {
            val otherCandidates = allocations.filter {
                !it.isLocked && !it.isCompleted && it.exercise.id != excludeExerciseId
            }
            val bestOther = otherCandidates.minByOrNull { it.exercise.stepQuantum * it.exercise.kcalPerUnit }
            if (bestOther != null) {
                val targetKcal = max(0, bestOther.calories + residual)
                val newUnits = quantizeUnits(bestOther.exercise, targetKcal)
                val newKcal = (newUnits * bestOther.exercise.kcalPerUnit).roundToInt()
                val residualAfterOther = abs(targetTotalKcal - (currentSum - bestOther.calories + newKcal))
                if (residualAfterOther <= 1) {
                    val idx = allocations.indexOfFirst { it.exercise.id == bestOther.exercise.id }
                    allocations[idx] = bestOther.copy(units = newUnits, calories = newKcal)
                    return
                }
            }
        }

        // 2. Fallback to all unlocked and uncompleted candidates to guarantee strict conservation <= 1 kcal
        val allCandidates = allocations.filter { !it.isLocked && !it.isCompleted }
        val absorber = allCandidates.minByOrNull { it.exercise.stepQuantum * it.exercise.kcalPerUnit }
            ?: return

        val absorberIndex = allocations.indexOfFirst { it.exercise.id == absorber.exercise.id }
        val targetKcal = max(0, absorber.calories + residual)
        val newUnits = quantizeUnits(absorber.exercise, targetKcal)
        val newKcal = (newUnits * absorber.exercise.kcalPerUnit).roundToInt()

        allocations[absorberIndex] = absorber.copy(units = newUnits, calories = newKcal)
    }
}
