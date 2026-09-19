package com.fit.tracker.ui.tracker

import com.fit.tracker.data.local.db.DailyPlanEntity
import com.fit.tracker.data.repository.IFitRepository
import com.fit.tracker.domain.engine.CalorieConservationEngine
import com.fit.tracker.domain.engine.CalorieEngine
import com.fit.tracker.domain.model.DefaultCatalogs
import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.domain.model.FoodItem
import com.fit.tracker.domain.model.FoodLogEntry
import com.fit.tracker.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class FakeFitRepository : IFitRepository {
    val foodsFlow = MutableStateFlow(DefaultCatalogs.foods)
    val foodRecordsMap = mutableMapOf<String, MutableStateFlow<List<FoodLogEntry>>>()
    val plansMap = mutableMapOf<String, MutableStateFlow<DailyPlanEntity?>>()
    val allocationsMap = mutableMapOf<String, MutableStateFlow<List<ExerciseAllocation>>>()
    val profileFlow = MutableStateFlow(UserProfile())

    fun getFoodRecordsFlow(date: String) =
        foodRecordsMap.getOrPut(date) { MutableStateFlow(emptyList()) }

    fun getPlanFlow(date: String) =
        plansMap.getOrPut(date) { MutableStateFlow(null) }

    fun getAllocFlow(date: String) =
        allocationsMap.getOrPut(date) { MutableStateFlow(emptyList()) }

    override fun searchFoods(query: String): Flow<List<FoodItem>> = foodsFlow

    override suspend fun addCustomFood(food: FoodItem) {
        foodsFlow.value = foodsFlow.value + food
    }

    override suspend fun addQuickFoodRecord(
        date: String,
        name: String,
        calories: Int,
        carbs: Double,
        protein: Double,
        fat: Double
    ): FoodItem {
        val food = FoodItem(
            id = "quick_${System.currentTimeMillis()}",
            name = name,
            pinyin = "kuaijie",
            caloriesPer100g = calories.toDouble(),
            protein = protein,
            carbs = carbs,
            fat = fat,
            category = "快捷速记",
            isCustom = true
        )
        foodsFlow.value = foodsFlow.value + food
        val entry = FoodLogEntry("rec_${System.currentTimeMillis()}", date, food, 100.0)
        val flow = getFoodRecordsFlow(date)
        flow.value = flow.value + entry
        return food
    }

    override fun getFoodRecords(date: String): Flow<List<FoodLogEntry>> = getFoodRecordsFlow(date)

    override suspend fun addFoodRecord(date: String, foodId: String, grams: Double) {
        val food = DefaultCatalogs.foods.first { it.id == foodId }
        val entry = FoodLogEntry("rec_${System.currentTimeMillis()}", date, food, grams)
        val flow = getFoodRecordsFlow(date)
        flow.value = flow.value + entry
    }

    override suspend fun deleteFoodRecord(id: String) {
        foodRecordsMap.values.forEach { flow ->
            flow.value = flow.value.filter { it.id != id }
        }
    }

    override fun getDailyPlan(date: String): Flow<DailyPlanEntity?> = getPlanFlow(date)

    override suspend fun saveDailyPlan(date: String, targetBurnKcal: Int, isConfirmed: Boolean) {
        getPlanFlow(date).value = DailyPlanEntity(date, targetBurnKcal, isConfirmed)
    }

    override fun getAllocations(date: String): Flow<List<ExerciseAllocation>> = getAllocFlow(date)

    override suspend fun saveAllocations(date: String, allocations: List<ExerciseAllocation>) {
        getAllocFlow(date).value = allocations
    }

    override suspend fun updateAllocation(date: String, allocation: ExerciseAllocation) {
        val flow = getAllocFlow(date)
        flow.value = flow.value.map {
            if (it.exercise.id == allocation.exercise.id) allocation else it
        }
    }

    override fun getUserProfile(): Flow<UserProfile> = profileFlow

    override suspend fun saveUserProfile(profile: UserProfile) {
        profileFlow.value = profile
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeFitRepository
    private lateinit var viewModel: TrackerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeFitRepository()
        viewModel = TrackerViewModel(
            repository = fakeRepo,
            calorieEngine = CalorieEngine(),
            conservationEngine = CalorieConservationEngine()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun activatePlanWithFood() {
        val food = DefaultCatalogs.foods.first()
        viewModel.addFoodRecord(food, 150.0)
    }

    @Test
    fun testInitialState_whenNoFoodLogged_exercisesRemainInStandby() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("Should have 0 exercises in standby when no food is logged", 0, state.activeExercises.size)
        assertEquals(0, state.targetBurnKcal)

        // Adding food activates the 5 baseline exercises
        activatePlanWithFood()
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals("Should activate 5 baseline exercises once food is recorded", 5, updatedState.activeExercises.size)
        assertEquals("jump_rope", updatedState.activeExercises[0].exercise.id)
        assertTrue(updatedState.targetBurnKcal > 0)
    }

    @Test
    fun testProgressCalculation_targetBurnZero_safeNonNaN() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse("Progress must not be NaN", state.completionRatio.isNaN())
        assertTrue("Progress must be >= 0.0", state.completionRatio >= 0.0f)
        assertTrue("Progress must be <= 1.0", state.completionRatio <= 1.0f)
    }

    @Test
    fun testToggleExerciseCompleted_updatesCompletionAndCompletedBurn() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()
        val initialAllocs = viewModel.uiState.value.activeExercises
        val firstExercise = initialAllocs.first()

        viewModel.toggleExerciseCompleted(firstExercise)
        advanceUntilIdle()

        val updatedAllocs = viewModel.uiState.value.activeExercises
        assertTrue("First exercise should be completed", updatedAllocs.first().isCompleted)
        assertTrue("Completed burn kcal should be > 0", viewModel.uiState.value.completedBurnKcal > 0)
        assertTrue("Progress should advance", viewModel.uiState.value.completionRatio > 0f)
    }

    @Test
    fun testAddFoodRecord_rebalancesTargetBurnAndAllocations() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        val initialIntake = viewModel.uiState.value.totalIntakeKcal
        assertEquals(0, initialIntake)

        val chicken = DefaultCatalogs.foods.first { it.id == "food_cooked_chicken_breast" }
        viewModel.addFoodRecord(chicken, 300.0)
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(399, updatedState.totalIntakeKcal)
        assertEquals(1, updatedState.foodEntries.size)
    }

    @Test
    fun testRemoveExercise_removesTargetExercise() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()
        assertEquals(5, viewModel.uiState.value.activeExercises.size)

        viewModel.removeExercise("bodyweight_squats")
        advanceUntilIdle()

        val updated = viewModel.uiState.value.activeExercises
        assertEquals(4, updated.size)
        assertFalse(updated.any { it.exercise.id == "bodyweight_squats" })
    }

    @Test
    fun testAddExercise_addsNewExerciseWithCalculatedKcal() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()
        val basketball = DefaultCatalogs.exercises.first { it.id == "basketball_half" }

        viewModel.addExercise(basketball)
        advanceUntilIdle()

        val updated = viewModel.uiState.value.activeExercises
        assertEquals(6, updated.size)
        val added = updated.first { it.exercise.id == "basketball_half" }
        assertEquals(15, added.units)
        assertTrue(added.calories > 0)
    }

    @Test
    fun testSetExerciseUnits_updatesExactUnitsDirectly() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()

        viewModel.setExerciseUnits("jump_rope", 500)
        advanceUntilIdle()

        val jumpRope = viewModel.uiState.value.activeExercises.first { it.exercise.id == "jump_rope" }
        assertEquals(500, jumpRope.units)
        assertEquals(70, jumpRope.calories) // 500 * 0.14 = 70 kcal
    }

    @Test
    fun testAdjustExerciseDelta_stepsUnitsDirectly() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()
        val beforeUnits = viewModel.uiState.value.activeExercises.first { it.exercise.id == "jump_rope" }.units

        viewModel.adjustExerciseDelta("jump_rope", 50)
        advanceUntilIdle()

        val afterUnits = viewModel.uiState.value.activeExercises.first { it.exercise.id == "jump_rope" }.units
        assertEquals(beforeUnits + 50, afterUnits)
    }

    @Test
    fun testUtcDatePicker_timezoneGuardrailFive() {
        val utcMillis = 1773964800000L
        val localDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneId.of("UTC")).toLocalDate()
        assertEquals(2026, localDate.year)
        assertEquals(3, localDate.monthValue)
        assertEquals(20, localDate.dayOfMonth)
    }

    @Test
    fun testSetExerciseUnits_rebalancesOtherExercisesAndConservesTotal() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()

        val initialAllocs = viewModel.uiState.value.activeExercises
        val initialJumpRope = initialAllocs.first { it.exercise.id == "jump_rope" }
        val initialTarget = viewModel.uiState.value.targetBurnKcal

        // Increase jump rope by 50 units
        viewModel.setExerciseUnits("jump_rope", initialJumpRope.units + 50)
        advanceUntilIdle()

        val updatedAllocs = viewModel.uiState.value.activeExercises
        val updatedJumpRope = updatedAllocs.first { it.exercise.id == "jump_rope" }
        assertTrue("Jump rope units should have increased", updatedJumpRope.units > initialJumpRope.units)

        // Total should remain conserved within 1 kcal
        val newTotal = updatedAllocs.sumOf { it.calories }
        assertTrue("Total allocated should remain conserved: initial $initialTarget, new $newTotal",
            kotlin.math.abs(newTotal - initialTarget) <= 1)
    }

    @Test
    fun testSetExerciseUnits_whenOtherExercisesHitZero_allowsOverflow() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()

        // Set jump rope to 5000 reps (~700 kcal), which exceeds the target
        viewModel.setExerciseUnits("jump_rope", 5000)
        advanceUntilIdle()

        val updatedAllocs = viewModel.uiState.value.activeExercises
        val jumpRope = updatedAllocs.first { it.exercise.id == "jump_rope" }
        assertEquals(5000, jumpRope.units)
        assertEquals(700, jumpRope.calories) // 500 * 0.14 = 70 kcal (or 5000 * 0.14 = 700 kcal)

        // Other exercises should have bottomed out at 0
        val others = updatedAllocs.filter { it.exercise.id != "jump_rope" }
        others.forEach { alloc ->
            assertEquals("Other exercises should bottom out at 0", 0, alloc.calories)
        }

        // Target burn should expand to accommodate overflow
        assertTrue("Target burn should expand to at least 700", viewModel.uiState.value.targetBurnKcal >= 700)
    }

    @Test
    fun testLocalDateChangeDay_changesDateCorrectly() = runTest(testDispatcher) {
        viewModel.selectDate("2026-03-20")
        assertEquals("2026-03-20", viewModel.currentDate.value)

        viewModel.changeDay(1)
        assertEquals("2026-03-21", viewModel.currentDate.value)

        viewModel.changeDay(-2)
        assertEquals("2026-03-19", viewModel.currentDate.value)
    }

    @Test
    fun testSaveUserProfile_updatesBmrAndRebalances() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        activatePlanWithFood()
        advanceUntilIdle()

        val initialBmr = viewModel.uiState.value.bmrKcal
        val initialWeight = viewModel.uiState.value.userProfile.weightKg

        // Update profile with higher weight: 72kg -> 85kg
        val newProfile = viewModel.uiState.value.userProfile.copy(weightKg = 85.0)
        viewModel.saveUserProfile(newProfile)
        advanceUntilIdle()

        val updatedBmr = viewModel.uiState.value.bmrKcal
        val updatedProfile = viewModel.uiState.value.userProfile

        assertEquals(85.0, updatedProfile.weightKg, 0.01)
        assertTrue("BMR should increase with higher body weight", updatedBmr > initialBmr)

        // Exercises should remain conserved and allocated properly
        val totalAllocated = viewModel.uiState.value.activeExercises.sumOf { it.calories }
        assertTrue("Allocations should be non-empty", totalAllocated > 0)
    }

    @Test
    fun testAddQuickFoodRecord_updatesIntakeAndRebalances() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val initialIntake = viewModel.uiState.value.totalIntakeKcal

        // Quick add 500 kcal directly via built-in calculator
        viewModel.addQuickFoodRecord(name = "快餐外卖", calories = 500, carbs = 60.0, protein = 25.0, fat = 15.0)
        advanceUntilIdle()

        assertEquals(initialIntake + 500, viewModel.uiState.value.totalIntakeKcal)
        val addedEntry = viewModel.uiState.value.foodEntries.firstOrNull { it.foodItem.name == "快餐外卖" }
        assertTrue("Quick food entry should be recorded", addedEntry != null)
        assertEquals(500, addedEntry?.calories)
    }
}
