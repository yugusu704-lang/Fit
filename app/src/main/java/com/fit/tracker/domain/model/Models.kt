package com.fit.tracker.domain.model

import kotlin.math.roundToInt

enum class Gender {
    MALE, FEMALE
}

enum class ActivityLevel(val multiplier: Double, val description: String) {
    SEDENTARY(1.2, "久坐少动（办公室工作）"),
    LIGHT(1.375, "轻度活动（每周轻量运动1-3天）"),
    MODERATE(1.55, "中度活动（每周中等运动3-5天）"),
    VERY_ACTIVE(1.725, "高度活跃（每周高强度运动6-7天）")
}

data class UserProfile(
    val gender: Gender = Gender.MALE,
    val age: Int = 28,
    val heightCm: Double = 175.0,
    val weightKg: Double = 72.0,
    val activityLevel: ActivityLevel = ActivityLevel.LIGHT,
    val targetDeficitKcal: Int = 400
)

data class FoodItem(
    val id: String,
    val name: String,
    val pinyin: String,
    val caloriesPer100g: Double,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val category: String = "主食谷物",
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)

data class FoodLogEntry(
    val id: String,
    val date: String, // YYYY-MM-DD
    val foodItem: FoodItem,
    val grams: Double
) {
    val calories: Int
        get() = ((foodItem.caloriesPer100g * grams) / 100.0).roundToInt()
}

enum class ExerciseUnitType(val label: String) {
    REPS("个"),
    LAPS("圈(400m)"),
    METERS("米"),
    KILOMETERS("公里"),
    MINUTES("分钟")
}

data class ExerciseDefinition(
    val id: String,
    val name: String,
    val unitType: ExerciseUnitType,
    val unitLabel: String,
    val kcalPerUnit: Double,
    val stepQuantum: Int,
    val minUnits: Int = 0,
    val category: String = "经典有氧",
    val isArchived: Boolean = false
)

data class ExerciseAllocation(
    val exercise: ExerciseDefinition,
    val units: Int,
    val calories: Int,
    val isLocked: Boolean = false,
    val isCompleted: Boolean = false
)

data class DailyTrackerState(
    val date: String,
    val userProfile: UserProfile = UserProfile(),
    val foodEntries: List<FoodLogEntry> = emptyList(),
    val totalIntakeKcal: Int = 0,
    val bmrKcal: Int = 0,
    val tdeeKcal: Int = 0,
    val targetBurnKcal: Int = 0,
    val activeExercises: List<ExerciseAllocation> = emptyList(),
    val isPlanConfirmed: Boolean = false,
    val isDrawerExpanded: Boolean = false
) {
    val completedBurnKcal: Int
        get() = activeExercises.filter { it.isCompleted }.sumOf { it.calories }

    val completionRatio: Float
        get() = if (targetBurnKcal > 0) {
            (completedBurnKcal.toFloat() / targetBurnKcal.toFloat()).coerceIn(0f, 1f)
        } else {
            val total = activeExercises.size
            if (total > 0) {
                (activeExercises.count { it.isCompleted }.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
}
