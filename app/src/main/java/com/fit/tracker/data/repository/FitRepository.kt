package com.fit.tracker.data.repository

import android.content.Context
import com.fit.tracker.data.local.db.DailyPlanEntity
import com.fit.tracker.data.local.db.ExerciseAllocationEntity
import com.fit.tracker.data.local.db.FitDatabase
import com.fit.tracker.data.local.db.FoodEntity
import com.fit.tracker.data.local.db.FoodRecordEntity
import com.fit.tracker.data.local.db.UserProfileEntity
import com.fit.tracker.domain.model.ActivityLevel
import com.fit.tracker.domain.model.DefaultCatalogs
import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.domain.model.FoodItem
import com.fit.tracker.domain.model.FoodLogEntry
import com.fit.tracker.domain.model.Gender
import com.fit.tracker.domain.model.UserProfile
import com.fit.tracker.widget.FitWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

interface IFitRepository {
    fun searchFoods(query: String): Flow<List<FoodItem>>
    suspend fun addCustomFood(food: FoodItem)
    suspend fun addQuickFoodRecord(date: String, name: String, calories: Int, carbs: Double = 0.0, protein: Double = 0.0, fat: Double = 0.0): FoodItem
    fun getFoodRecords(date: String): Flow<List<FoodLogEntry>>
    suspend fun addFoodRecord(date: String, foodId: String, grams: Double)
    suspend fun deleteFoodRecord(id: String)
    fun getDailyPlan(date: String): Flow<DailyPlanEntity?>
    suspend fun saveDailyPlan(date: String, targetBurnKcal: Int, isConfirmed: Boolean)
    fun getAllocations(date: String): Flow<List<ExerciseAllocation>>
    suspend fun saveAllocations(date: String, allocations: List<ExerciseAllocation>)
    suspend fun updateAllocation(date: String, allocation: ExerciseAllocation)
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)
}

class FitRepository(
    private val db: FitDatabase,
    private val context: Context? = null
) : IFitRepository {

    private val foodDao = db.foodDao()
    private val recordDao = db.foodRecordDao()
    private val planDao = db.dailyPlanDao()
    private val allocationDao = db.exerciseAllocationDao()
    private val profileDao = db.userProfileDao()

    private fun notifyWidget() {
        context?.let { FitWidgetProvider.notifyWidgetsUpdate(it) }
    }

    override fun searchFoods(query: String): Flow<List<FoodItem>> {
        val flow = if (query.isBlank()) foodDao.getAllFoods() else foodDao.searchFoods(query.trim())
        return flow.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addCustomFood(food: FoodItem) {
        foodDao.insertFood(
            FoodEntity(
                id = food.id.ifBlank { "custom_" + UUID.randomUUID().toString() },
                name = food.name,
                pinyin = food.pinyin,
                caloriesPer100g = food.caloriesPer100g,
                protein = food.protein,
                carbs = food.carbs,
                fat = food.fat,
                isCustom = true,
                isArchived = false
            )
        )
    }

    override suspend fun addQuickFoodRecord(
        date: String,
        name: String,
        calories: Int,
        carbs: Double,
        protein: Double,
        fat: Double
    ): FoodItem {
        val foodId = "quick_" + UUID.randomUUID().toString().take(8)
        val cleanName = name.trim().ifBlank { "快捷摄入" }
        val foodItem = FoodItem(
            id = foodId,
            name = cleanName,
            pinyin = "kuaijie",
            caloriesPer100g = calories.toDouble(),
            protein = protein,
            carbs = carbs,
            fat = fat,
            category = "快捷速记",
            isCustom = true
        )
        addCustomFood(foodItem)
        addFoodRecord(date, foodId, 100.0)
        return foodItem
    }

    override fun getFoodRecords(date: String): Flow<List<FoodLogEntry>> {
        return combine(recordDao.getRecordsByDate(date), foodDao.getAllFoods()) { records, foods ->
            val foodMap = foods.associateBy { it.id }
            records.mapNotNull { record ->
                val foodEntity = foodMap[record.foodId] ?: return@mapNotNull null
                FoodLogEntry(
                    id = record.id,
                    date = record.date,
                    foodItem = foodEntity.toDomain(),
                    grams = record.grams
                )
            }
        }
    }

    override suspend fun addFoodRecord(date: String, foodId: String, grams: Double) {
        val record = FoodRecordEntity(
            id = UUID.randomUUID().toString(),
            date = date,
            foodId = foodId,
            grams = grams
        )
        recordDao.insertRecord(record)
        notifyWidget()
    }

    override suspend fun deleteFoodRecord(id: String) {
        recordDao.deleteRecord(id)
        notifyWidget()
    }

    override fun getDailyPlan(date: String): Flow<DailyPlanEntity?> = planDao.getPlanByDate(date)

    override suspend fun saveDailyPlan(date: String, targetBurnKcal: Int, isConfirmed: Boolean) {
        planDao.insertOrUpdatePlan(
            DailyPlanEntity(
                date = date,
                targetBurnKcal = targetBurnKcal,
                isConfirmed = isConfirmed
            )
        )
        notifyWidget()
    }

    override fun getAllocations(date: String): Flow<List<ExerciseAllocation>> {
        return allocationDao.getAllocationsByDate(date).map { entities ->
            val catalogMap = DefaultCatalogs.exercises.associateBy { it.id }
            entities.mapNotNull { entity ->
                val def = catalogMap[entity.exerciseId] ?: return@mapNotNull null
                ExerciseAllocation(
                    exercise = def,
                    units = entity.units,
                    calories = entity.calories,
                    isLocked = entity.isLocked,
                    isCompleted = entity.isCompleted
                )
            }
        }
    }

    override suspend fun saveAllocations(date: String, allocations: List<ExerciseAllocation>) {
        val entities = allocations.map { alloc ->
            ExerciseAllocationEntity(
                id = "${date}_${alloc.exercise.id}",
                date = date,
                exerciseId = alloc.exercise.id,
                units = alloc.units,
                calories = alloc.calories,
                isLocked = alloc.isLocked,
                isCompleted = alloc.isCompleted
            )
        }
        allocationDao.replaceAllocationsForDate(date, entities)
        notifyWidget()
    }

    override suspend fun updateAllocation(date: String, allocation: ExerciseAllocation) {
        allocationDao.updateAllocation(
            ExerciseAllocationEntity(
                id = "${date}_${allocation.exercise.id}",
                date = date,
                exerciseId = allocation.exercise.id,
                units = allocation.units,
                calories = allocation.calories,
                isLocked = allocation.isLocked,
                isCompleted = allocation.isCompleted
            )
        )
        notifyWidget()
    }

    override fun getUserProfile(): Flow<UserProfile> {
        return profileDao.getUserProfile().map { entity ->
            if (entity == null) {
                UserProfile()
            } else {
                UserProfile(
                    gender = runCatching { Gender.valueOf(entity.gender) }.getOrDefault(Gender.MALE),
                    age = entity.age,
                    heightCm = entity.heightCm,
                    weightKg = entity.weightKg,
                    activityLevel = runCatching { ActivityLevel.valueOf(entity.activityLevel) }.getOrDefault(ActivityLevel.LIGHT),
                    targetDeficitKcal = entity.targetDeficitKcal
                )
            }
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        profileDao.insertOrUpdateProfile(
            UserProfileEntity(
                id = 1,
                gender = profile.gender.name,
                age = profile.age,
                heightCm = profile.heightCm,
                weightKg = profile.weightKg,
                activityLevel = profile.activityLevel.name,
                targetDeficitKcal = profile.targetDeficitKcal
            )
        )
        notifyWidget()
    }

    private fun FoodEntity.toDomain() = FoodItem(
        id = id,
        name = name,
        pinyin = pinyin,
        caloriesPer100g = caloriesPer100g,
        protein = protein,
        carbs = carbs,
        fat = fat,
        category = DefaultCatalogs.foodCategoryMap[id] ?: "主食谷物",
        isCustom = isCustom,
        isArchived = isArchived
    )
}
