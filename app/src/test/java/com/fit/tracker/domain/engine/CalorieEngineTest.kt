package com.fit.tracker.domain.engine

import com.fit.tracker.domain.model.ActivityLevel
import com.fit.tracker.domain.model.Gender
import com.fit.tracker.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieEngineTest {

    private val engine = CalorieEngine()

    @Test
    fun calculateBmr_maleProfile_returnsCorrectMifflinStJeor() {
        val profile = UserProfile(
            gender = Gender.MALE,
            age = 28,
            heightCm = 175.0,
            weightKg = 72.0
        )
        // 10 * 72 + 6.25 * 175 - 5 * 28 + 5 = 720 + 1093.75 - 140 + 5 = 1678.75 -> 1679
        val bmr = engine.calculateBmr(profile)
        assertEquals(1679, bmr)
    }

    @Test
    fun calculateBmr_femaleProfile_returnsCorrectMifflinStJeor() {
        val profile = UserProfile(
            gender = Gender.FEMALE,
            age = 25,
            heightCm = 165.0,
            weightKg = 55.0
        )
        // 10 * 55 + 6.25 * 165 - 5 * 25 - 161 = 550 + 1031.25 - 125 - 161 = 1295.25 -> 1295
        val bmr = engine.calculateBmr(profile)
        assertEquals(1295, bmr)
    }

    @Test
    fun calculateTdee_lightActivity_multipliesCorrectly() {
        val profile = UserProfile(
            gender = Gender.MALE,
            age = 28,
            heightCm = 175.0,
            weightKg = 72.0,
            activityLevel = ActivityLevel.LIGHT // 1.375
        )
        val bmr = 1679
        // 1679 * 1.375 = 2308.625 -> 2309
        val tdee = engine.calculateTdee(bmr, profile.activityLevel)
        assertEquals(2309, tdee)
    }

    @Test
    fun calculateTargetBurn_overBudget_calculatesAccurateBurn() {
        val tdee = 2309
        val deficitGoal = 400
        val intake = 2300 // Diet budget is 2309 - 400 = 1909 kcal. Over by 391 kcal.
        val targetBurn = engine.calculateTargetBurn(
            totalIntakeKcal = intake,
            tdee = tdee,
            targetDeficitKcal = deficitGoal
        )
        assertEquals(391, targetBurn)
    }

    @Test
    fun calculateTargetBurn_underBudget_returnsZeroBurn() {
        val tdee = 2309
        val deficitGoal = 400
        val intake = 1800 // Under budget (1909), no mandatory workout required to hit deficit
        val targetBurn = engine.calculateTargetBurn(
            totalIntakeKcal = intake,
            tdee = tdee,
            targetDeficitKcal = deficitGoal
        )
        assertEquals(0, targetBurn)
    }
}
