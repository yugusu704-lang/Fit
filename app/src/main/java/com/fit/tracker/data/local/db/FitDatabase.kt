package com.fit.tracker.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fit.tracker.domain.model.DefaultCatalogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FoodEntity::class,
        FoodRecordEntity::class,
        DailyPlanEntity::class,
        ExerciseAllocationEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FitDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun foodRecordDao(): FoodRecordDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun exerciseAllocationDao(): ExerciseAllocationDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: FitDatabase? = null

        fun getInstance(context: Context): FitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitDatabase::class.java,
                    "fit_local.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default foods and default user profile
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            val initialFoods = DefaultCatalogs.foods.map { food ->
                                FoodEntity(
                                    id = food.id,
                                    name = food.name,
                                    pinyin = food.pinyin,
                                    caloriesPer100g = food.caloriesPer100g,
                                    protein = food.protein,
                                    carbs = food.carbs,
                                    fat = food.fat,
                                    isCustom = food.isCustom,
                                    isArchived = food.isArchived
                                )
                            }
                            database.foodDao().insertFoods(initialFoods)

                            // Initial Profile: 28yo male, 72kg, 175cm, light activity, 400 kcal deficit
                            database.userProfileDao().insertOrUpdateProfile(
                                UserProfileEntity(
                                    id = 1,
                                    gender = "MALE",
                                    age = 28,
                                    heightCm = 175.0,
                                    weightKg = 72.0,
                                    activityLevel = "LIGHT",
                                    targetDeficitKcal = 400
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
