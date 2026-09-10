package com.fit.tracker.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE isArchived = 0 AND (name LIKE '%' || :query || '%' OR pinyin LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: String): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFoods(foods: List<FoodEntity>)
}

@Dao
interface FoodRecordDao {
    @Query("SELECT * FROM food_records WHERE date = :date")
    fun getRecordsByDate(date: String): Flow<List<FoodRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FoodRecordEntity)

    @Query("DELETE FROM food_records WHERE id = :id")
    suspend fun deleteRecord(id: String)
}

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    fun getPlanByDate(date: String): Flow<DailyPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlan(plan: DailyPlanEntity)
}

@Dao
interface ExerciseAllocationDao {
    @Query("SELECT * FROM exercise_allocations WHERE date = :date")
    fun getAllocationsByDate(date: String): Flow<List<ExerciseAllocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<ExerciseAllocationEntity>)

    @Update
    suspend fun updateAllocation(allocation: ExerciseAllocationEntity)

    @Query("DELETE FROM exercise_allocations WHERE date = :date")
    suspend fun deleteAllocationsByDate(date: String)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
