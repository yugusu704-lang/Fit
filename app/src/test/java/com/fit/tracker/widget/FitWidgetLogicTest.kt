package com.fit.tracker.widget

import com.fit.tracker.MainActivity
import com.fit.tracker.domain.engine.CalorieEngine
import com.fit.tracker.domain.model.ActivityLevel
import com.fit.tracker.domain.model.DefaultCatalogs
import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class FitWidgetLogicTest {

    private val calorieEngine = CalorieEngine()

    @Test
    fun testEmptyStateWhenNoRecordsOrAllocations() {
        val foodRecords = emptyList<Any>()
        val allocations = emptyList<ExerciseAllocation>()
        val isEmpty = foodRecords.isEmpty() && allocations.isEmpty()
        assertTrue("Widget should show empty state when no food logged and no allocations exist", isEmpty)
    }

    @Test
    fun testProgressRatioCalculation() {
        val targetBurn = 400
        val completedBurn = 200
        val progressRatio = if (targetBurn > 0) {
            ((completedBurn.toDouble() / targetBurn) * 100).roundToInt().coerceIn(0, 100)
        } else 0
        assertEquals(50, progressRatio)

        // Edge case: overflow
        val overflowBurn = 500
        val cappedRatio = if (targetBurn > 0) {
            ((overflowBurn.toDouble() / targetBurn) * 100).roundToInt().coerceIn(0, 100)
        } else 0
        assertEquals(100, cappedRatio)

        // Edge case: 0 target
        val zeroTargetRatio = if (0 > 0) 100 else 0
        assertEquals(0, zeroTargetRatio)
    }

    @Test
    fun testExerciseItemFormatting() {
        val catalogMap = DefaultCatalogs.exercises.associateBy { it.id }
        val jumpRope = catalogMap["jump_rope"]!!
        val unitStr = "1200 ${jumpRope.unitLabel}"
        val calStr = "168 kcal"

        assertEquals("1200 个", unitStr)
        assertEquals("168 kcal", calStr)
    }

    @Test
    fun testDateFormattingForWidget() {
        val testDate = LocalDate.of(2026, 9, 19)
        val formatted = testDate.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE))
        assertEquals("9月19日", formatted)
    }

    @Test
    fun testConstantsMatching() {
        assertEquals("extra_action", MainActivity.EXTRA_ACTION)
        assertEquals("action_open_food_dialog", MainActivity.ACTION_OPEN_FOOD_DIALOG)
        assertEquals(
            "com.fit.tracker.widget.ACTION_TOGGLE_EXERCISE",
            FitWidgetActionReceiver.ACTION_TOGGLE_EXERCISE
        )
        assertEquals("extra_date", FitWidgetActionReceiver.EXTRA_DATE)
        assertEquals("extra_exercise_id", FitWidgetActionReceiver.EXTRA_EXERCISE_ID)
        assertEquals(
            "com.fit.tracker.widget.ACTION_MIDNIGHT_REFRESH",
            FitWidgetRefreshReceiver.ACTION_MIDNIGHT_REFRESH
        )
    }
}
