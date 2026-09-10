package com.fit.tracker.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pinyin: String,
    val caloriesPer100g: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val isCustom: Boolean,
    val isArchived: Boolean
)

@Entity(tableName = "food_records")
data class FoodRecordEntity(
    @PrimaryKey val id: String,
    val date: String, // YYYY-MM-DD
    val foodId: String,
    val grams: Double
)

@Entity(tableName = "daily_plans")
data class DailyPlanEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val targetBurnKcal: Int,
    val isConfirmed: Boolean
)

@Entity(tableName = "exercise_allocations")
data class ExerciseAllocationEntity(
    @PrimaryKey val id: String, // "$date-$exerciseId"
    val date: String,
    val exerciseId: String,
    val units: Int,
    val calories: Int,
    val isLocked: Boolean,
    val isCompleted: Boolean
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val gender: String,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: String,
    val targetDeficitKcal: Int
)
