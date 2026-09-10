package com.fit.tracker.domain.engine

import com.fit.tracker.domain.model.DefaultCatalogs
import com.fit.tracker.domain.model.ExerciseAllocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CalorieConservationEngineTest {

    private val engine = CalorieConservationEngine()

    private val jumpRope = DefaultCatalogs.exercises.first { it.id == "jump_rope" } // 0.14 kcal/rep, step 10
    private val running = DefaultCatalogs.exercises.first { it.id == "running_400m" } // 32 kcal/lap, step 1
    private val squats = DefaultCatalogs.exercises.first { it.id == "bodyweight_squats" } // 0.32 kcal/rep, step 5

    @Test
    fun initialAllocation_twoExercises_splitsTargetAndConservesCalories() {
        val targetKcal = 300
        val selected = listOf(jumpRope, running)

        val allocations = engine.allocateInitial(selected, targetKcal)

        assertEquals(2, allocations.size)
        val totalAllocated = allocations.sumOf { it.calories }
        // Conserved within 1 kcal
        assertTrue("Total allocated $totalAllocated should be within 1 kcal of target $targetKcal",
            abs(totalAllocated - targetKcal) <= 1)

        // Ensure physical units adhere to stepQuantum
        allocations.forEach { alloc ->
            assertEquals("Units must be multiple of stepQuantum",
                0, alloc.units % alloc.exercise.stepQuantum)
        }
    }

    @Test
    fun rebalance_twoExercises_adjustingOneDirectlyOffsetsTheOther() {
        val targetKcal = 300
        val initial = engine.allocateInitial(listOf(jumpRope, running), targetKcal)
        val initialJumpRope = initial.first { it.exercise.id == jumpRope.id }
        val initialRunning = initial.first { it.exercise.id == running.id }

        // User increases jump rope by approx 50 kcal
        val targetJumpRopeKcal = initialJumpRope.calories + 50
        val rebalanced = engine.rebalance(
            allocations = initial,
            targetTotalKcal = targetKcal,
            modifiedExerciseId = jumpRope.id,
            requestedCalories = targetJumpRopeKcal
        )

        val updatedJumpRope = rebalanced.first { it.exercise.id == jumpRope.id }
        val updatedRunning = rebalanced.first { it.exercise.id == running.id }

        assertTrue("Jump rope calories should have increased", updatedJumpRope.calories > initialJumpRope.calories)
        assertTrue("Running calories should have decreased", updatedRunning.calories < initialRunning.calories)

        val totalAllocated = rebalanced.sumOf { it.calories }
        assertTrue("Total must remain conserved within 1 kcal of $targetKcal, got $totalAllocated",
            abs(totalAllocated - targetKcal) <= 1)
    }

    @Test
    fun rebalance_threeExercisesWithLock_respectsLockedItem() {
        val targetKcal = 450
        val initial = engine.allocateInitial(listOf(jumpRope, running, squats), targetKcal)

        // Lock running
        val withLockedRunning = initial.map {
            if (it.exercise.id == running.id) it.copy(isLocked = true) else it
        }
        val lockedRunningCalories = withLockedRunning.first { it.exercise.id == running.id }.calories

        // Modify jump rope
        val rebalanced = engine.rebalance(
            allocations = withLockedRunning,
            targetTotalKcal = targetKcal,
            modifiedExerciseId = jumpRope.id,
            requestedCalories = 100
        )

        val rebalancedRunning = rebalanced.first { it.exercise.id == running.id }
        assertEquals("Locked item calories must not change", lockedRunningCalories, rebalancedRunning.calories)

        val totalAllocated = rebalanced.sumOf { it.calories }
        assertTrue("Total must remain conserved within 1 kcal of $targetKcal",
            abs(totalAllocated - targetKcal) <= 1)
    }

    @Test
    fun rebalance_completedExercise_isImmutable() {
        val targetKcal = 300
        val initial = engine.allocateInitial(listOf(jumpRope, running), targetKcal)

        // Mark jump rope completed
        val withCompletedJumpRope = initial.map {
            if (it.exercise.id == jumpRope.id) it.copy(isCompleted = true) else it
        }
        val completedKcal = withCompletedJumpRope.first { it.exercise.id == jumpRope.id }.calories

        // Attempt to rebalance
        val rebalanced = engine.rebalance(
            allocations = withCompletedJumpRope,
            targetTotalKcal = 400, // Mid-day intake increased target from 300 to 400
            modifiedExerciseId = running.id,
            requestedCalories = 400 - completedKcal
        )

        val finalJumpRope = rebalanced.first { it.exercise.id == jumpRope.id }
        assertEquals("Completed item calories must remain immutable", completedKcal, finalJumpRope.calories)
        assertTrue(finalJumpRope.isCompleted)
    }

    @Test
    fun rebalance_floorClamping_doesNotAllowNegativeCalories() {
        val targetKcal = 200
        val initial = engine.allocateInitial(listOf(jumpRope, running), targetKcal)

        // Request an impossible 500 kcal for jump rope when total is 200
        val rebalanced = engine.rebalance(
            allocations = initial,
            targetTotalKcal = targetKcal,
            modifiedExerciseId = jumpRope.id,
            requestedCalories = 500
        )

        val updatedRunning = rebalanced.first { it.exercise.id == running.id }
        assertTrue("Running cannot be negative", updatedRunning.calories >= 0)
        assertTrue("Running units cannot be negative", updatedRunning.units >= 0)

        val total = rebalanced.sumOf { it.calories }
        assertTrue("Total must remain conserved", abs(total - targetKcal) <= 1)
    }
}
