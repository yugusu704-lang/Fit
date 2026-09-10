package com.fit.tracker.domain.engine

import com.fit.tracker.domain.model.ActivityLevel
import com.fit.tracker.domain.model.Gender
import com.fit.tracker.domain.model.UserProfile
import kotlin.math.max
import kotlin.math.roundToInt

class CalorieEngine {

    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor equation.
     */
    fun calculateBmr(profile: UserProfile): Int {
        val base = (10.0 * profile.weightKg) + (6.25 * profile.heightCm) - (5.0 * profile.age)
        val bmr = when (profile.gender) {
            Gender.MALE -> base + 5.0
            Gender.FEMALE -> base - 161.0
        }
        return bmr.roundToInt()
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE) based on activity level.
     */
    fun calculateTdee(bmr: Int, activityLevel: ActivityLevel): Int {
        return (bmr.toDouble() * activityLevel.multiplier).roundToInt()
    }

    /**
     * Calculate target calories to burn via exercise today.
     * Diet budget = TDEE - targetDeficit.
     * Target Burn = max(0, totalIntake - Diet budget).
     * If intake exceeds the budget, exercise is prescribed to make up the deficit.
     */
    fun calculateTargetBurn(
        totalIntakeKcal: Int,
        tdee: Int,
        targetDeficitKcal: Int
    ): Int {
        val dietBudget = tdee - targetDeficitKcal
        return max(0, totalIntakeKcal - dietBudget)
    }
}
