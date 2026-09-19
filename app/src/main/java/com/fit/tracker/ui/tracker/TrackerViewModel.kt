package com.fit.tracker.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit.tracker.data.repository.IFitRepository
import com.fit.tracker.domain.engine.CalorieConservationEngine
import com.fit.tracker.domain.engine.CalorieEngine
import com.fit.tracker.domain.model.DailyTrackerState
import com.fit.tracker.domain.model.DefaultCatalogs
import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.domain.model.ExerciseDefinition
import com.fit.tracker.domain.model.ExerciseUnitType
import com.fit.tracker.domain.model.FoodItem
import com.fit.tracker.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModel(
    private val repository: IFitRepository,
    private val calorieEngine: CalorieEngine = CalorieEngine(),
    private val conservationEngine: CalorieConservationEngine = CalorieConservationEngine()
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _currentDate = MutableStateFlow(LocalDate.now().format(dateFormatter))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    val allFoods: StateFlow<List<FoodItem>> = repository.searchFoods("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultCatalogs.foods)

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    val uiState: StateFlow<DailyTrackerState> = _currentDate.flatMapLatest { date ->
        combine(
            repository.getUserProfile(),
            repository.getFoodRecords(date),
            repository.getAllocations(date),
            repository.getDailyPlan(date)
        ) { profile, foodRecords, allocations, plan ->
            val bmr = calorieEngine.calculateBmr(profile)
            val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
            val totalIntake = foodRecords.sumOf { it.calories }
            val targetBurn = calorieEngine.calculateTargetBurn(totalIntake, tdee, profile.targetDeficitKcal)

            // When no food is logged yet and no custom plan exists, keep exercises empty (standby state)
            val effectiveExercises = if (allocations.isEmpty() && plan == null) {
                if (foodRecords.isEmpty()) {
                    emptyList()
                } else {
                    val baselineBurn = if (targetBurn > 0) targetBurn else 300
                    conservationEngine.allocateInitial(DefaultCatalogs.exercises.take(5), baselineBurn)
                }
            } else {
                allocations
            }

            DailyTrackerState(
                date = date,
                userProfile = profile,
                foodEntries = foodRecords,
                totalIntakeKcal = totalIntake,
                bmrKcal = bmr,
                tdeeKcal = tdee,
                targetBurnKcal = if (foodRecords.isEmpty() && plan == null && allocations.isEmpty()) 0 else (if (targetBurn > 0) targetBurn else effectiveExercises.sumOf { it.calories }),
                activeExercises = effectiveExercises,
                isPlanConfirmed = plan?.isConfirmed ?: false,
                isDrawerExpanded = false
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        DailyTrackerState(date = _currentDate.value)
    )

    val availableExercises: List<ExerciseDefinition> = DefaultCatalogs.exercises

    init {
        ensureDailyPlanInitialized(_currentDate.value)
    }

    private fun ensureDailyPlanInitialized(date: String) {
        viewModelScope.launch {
            val plan = repository.getDailyPlan(date).firstOrNull()
            val allocations = repository.getAllocations(date).firstOrNull() ?: emptyList()
            val foodRecords = repository.getFoodRecords(date).firstOrNull() ?: emptyList()

            // Only initialize automatic baseline if user has logged foods and no plan exists
            if (plan == null && allocations.isEmpty() && foodRecords.isNotEmpty()) {
                val profile = repository.getUserProfile().firstOrNull() ?: UserProfile()
                val bmr = calorieEngine.calculateBmr(profile)
                val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
                val totalIntake = foodRecords.sumOf { it.calories }
                val targetBurn = calorieEngine.calculateTargetBurn(totalIntake, tdee, profile.targetDeficitKcal)
                val baselineBurn = if (targetBurn > 0) targetBurn else 300
                val initialAllocs = conservationEngine.allocateInitial(DefaultCatalogs.exercises.take(5), baselineBurn)
                repository.saveDailyPlan(date, baselineBurn, false)
                repository.saveAllocations(date, initialAllocs)
            }
        }
    }

    fun selectDate(date: String) {
        _currentDate.value = date
        ensureDailyPlanInitialized(date)
    }

    fun changeDay(delta: Int) {
        val current = runCatching { LocalDate.parse(_currentDate.value, dateFormatter) }.getOrDefault(LocalDate.now())
        val nextDate = current.plusDays(delta.toLong()).format(dateFormatter)
        selectDate(nextDate)
    }

    fun addFoodRecord(food: FoodItem, grams: Double) {
        viewModelScope.launch {
            _isCalculating.value = true
            val date = _currentDate.value
            repository.addFoodRecord(date, food.id, grams)

            // Dynamic activation or re-balancing of target exercises
            val currentAllocations = repository.getAllocations(date).firstOrNull() ?: emptyList()
            val profile = uiState.value.userProfile
            val bmr = calorieEngine.calculateBmr(profile)
            val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
            val newIntake = uiState.value.totalIntakeKcal + ((food.caloriesPer100g * grams) / 100.0).roundToInt()
            val newTargetBurn = calorieEngine.calculateTargetBurn(newIntake, tdee, profile.targetDeficitKcal)
            val burnTarget = if (newTargetBurn > 0) newTargetBurn else 300

            val rebalanced = if (currentAllocations.isEmpty()) {
                conservationEngine.allocateInitial(DefaultCatalogs.exercises.take(5), burnTarget)
            } else {
                conservationEngine.rebalanceForNewTarget(currentAllocations, burnTarget)
            }
            repository.saveAllocations(date, rebalanced)
            repository.saveDailyPlan(date, burnTarget, true)
            _isCalculating.value = false
        }
    }

    fun addQuickFoodRecord(
        name: String,
        calories: Int,
        carbs: Double = 0.0,
        protein: Double = 0.0,
        fat: Double = 0.0
    ) {
        viewModelScope.launch {
            _isCalculating.value = true
            val date = _currentDate.value
            repository.addQuickFoodRecord(date, name, calories, carbs, protein, fat)

            val currentAllocations = repository.getAllocations(date).firstOrNull() ?: emptyList()
            val profile = uiState.value.userProfile
            val bmr = calorieEngine.calculateBmr(profile)
            val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
            val newIntake = uiState.value.totalIntakeKcal + calories
            val newTargetBurn = calorieEngine.calculateTargetBurn(newIntake, tdee, profile.targetDeficitKcal)
            val burnTarget = if (newTargetBurn > 0) newTargetBurn else 300

            val rebalanced = if (currentAllocations.isEmpty()) {
                conservationEngine.allocateInitial(DefaultCatalogs.exercises.take(5), burnTarget)
            } else {
                conservationEngine.rebalanceForNewTarget(currentAllocations, burnTarget)
            }
            repository.saveAllocations(date, rebalanced)
            repository.saveDailyPlan(date, burnTarget, true)
            _isCalculating.value = false
        }
    }

    fun deleteFoodRecord(recordId: String) {
        viewModelScope.launch {
            _isCalculating.value = true
            repository.deleteFoodRecord(recordId)
            val date = _currentDate.value
            val remainingRecords = repository.getFoodRecords(date).firstOrNull() ?: emptyList()
            if (remainingRecords.isEmpty()) {
                repository.saveAllocations(date, emptyList())
                repository.saveDailyPlan(date, 0, false)
            } else {
                val currentAllocations = repository.getAllocations(date).firstOrNull() ?: emptyList()
                val profile = uiState.value.userProfile
                val bmr = calorieEngine.calculateBmr(profile)
                val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
                val newIntake = remainingRecords.sumOf { it.calories }
                val newTargetBurn = calorieEngine.calculateTargetBurn(newIntake, tdee, profile.targetDeficitKcal)
                val burnTarget = if (newTargetBurn > 0) newTargetBurn else 300
                val rebalanced = conservationEngine.rebalanceForNewTarget(currentAllocations, burnTarget)
                repository.saveAllocations(date, rebalanced)
                repository.saveDailyPlan(date, burnTarget, true)
            }
            _isCalculating.value = false
        }
    }

    fun toggleExerciseCompleted(allocation: ExerciseAllocation) {
        viewModelScope.launch {
            val date = _currentDate.value
            val updated = allocation.copy(isCompleted = !allocation.isCompleted)
            repository.updateAllocation(date, updated)
        }
    }

    fun adjustExerciseDelta(exerciseId: String, deltaUnits: Int) {
        val current = uiState.value.activeExercises
        val target = current.firstOrNull { it.exercise.id == exerciseId } ?: return
        val newUnits = kotlin.math.max(0, target.units + deltaUnits)
        setExerciseUnits(exerciseId, newUnits)
    }

    fun setExerciseUnits(exerciseId: String, newUnits: Int) {
        viewModelScope.launch {
            val date = _currentDate.value
            val current = uiState.value.activeExercises
            val target = current.firstOrNull { it.exercise.id == exerciseId } ?: return@launch
            val clampedUnits = kotlin.math.max(0, newUnits)
            val requestedCalories = (clampedUnits * target.exercise.kcalPerUnit).roundToInt()
            val currentTotalTarget = uiState.value.targetBurnKcal

            val rebalanced = conservationEngine.rebalance(
                allocations = current,
                targetTotalKcal = currentTotalTarget,
                modifiedExerciseId = exerciseId,
                requestedCalories = requestedCalories,
                allowOverflow = true
            )
            repository.saveAllocations(date, rebalanced)

            val newTotalBurn = rebalanced.sumOf { it.calories }
            if (newTotalBurn > currentTotalTarget) {
                repository.saveDailyPlan(date, newTotalBurn, true)
            }
        }
    }

    fun removeExercise(exerciseId: String) {
        viewModelScope.launch {
            val date = _currentDate.value
            val current = uiState.value.activeExercises
            val remaining = current.filter { it.exercise.id != exerciseId }
            repository.saveAllocations(date, remaining)
        }
    }

    fun addExercise(exercise: ExerciseDefinition) {
        viewModelScope.launch {
            val date = _currentDate.value
            val current = uiState.value.activeExercises
            if (current.any { it.exercise.id == exercise.id }) return@launch

            val defaultUnits = when (exercise.unitType) {
                ExerciseUnitType.REPS -> if (exercise.minUnits > 0) exercise.minUnits else exercise.stepQuantum * 10
                ExerciseUnitType.LAPS -> if (exercise.minUnits > 0) exercise.minUnits else 2
                ExerciseUnitType.METERS -> if (exercise.minUnits > 0) exercise.minUnits else 200
                ExerciseUnitType.KILOMETERS -> if (exercise.minUnits > 0) exercise.minUnits else 2
                ExerciseUnitType.MINUTES -> if (exercise.minUnits > 0) exercise.minUnits else 20
            }
            val calories = (defaultUnits * exercise.kcalPerUnit).roundToInt()
            val newAllocation = ExerciseAllocation(
                exercise = exercise,
                units = defaultUnits,
                calories = calories
            )
            repository.saveAllocations(date, current + newAllocation)
        }
    }

    fun replaceExercise(oldExerciseId: String, newExercise: ExerciseDefinition) {
        viewModelScope.launch {
            val date = _currentDate.value
            val current = uiState.value.activeExercises
            val oldItem = current.firstOrNull { it.exercise.id == oldExerciseId } ?: return@launch

            val rawUnits = oldItem.calories.toDouble() / newExercise.kcalPerUnit
            val stepCount = (rawUnits / newExercise.stepQuantum).roundToInt()
            val units = kotlin.math.max(newExercise.minUnits, stepCount * newExercise.stepQuantum)
            val kcal = (units * newExercise.kcalPerUnit).roundToInt()

            val updatedList = current.map {
                if (it.exercise.id == oldExerciseId) {
                    ExerciseAllocation(
                        exercise = newExercise,
                        units = units,
                        calories = kcal,
                        isLocked = oldItem.isLocked,
                        isCompleted = oldItem.isCompleted
                    )
                } else {
                    it
                }
            }
            repository.saveAllocations(date, updatedList)
        }
    }

    fun addCustomFood(name: String, kcalPer100g: Double) {
        viewModelScope.launch {
            val custom = FoodItem(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                pinyin = name,
                caloriesPer100g = kcalPer100g,
                isCustom = true
            )
            repository.addCustomFood(custom)
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isCalculating.value = true
            repository.saveUserProfile(profile)

            val date = _currentDate.value
            val currentAllocations = uiState.value.activeExercises
            if (currentAllocations.isNotEmpty()) {
                val bmr = calorieEngine.calculateBmr(profile)
                val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
                val totalIntake = uiState.value.totalIntakeKcal
                val newTargetBurn = calorieEngine.calculateTargetBurn(totalIntake, tdee, profile.targetDeficitKcal)
                if (newTargetBurn > 0) {
                    val rebalanced = conservationEngine.rebalanceForNewTarget(currentAllocations, newTargetBurn)
                    repository.saveAllocations(date, rebalanced)
                    repository.saveDailyPlan(date, newTargetBurn, true)
                }
            }
            _isCalculating.value = false
        }
    }

    class Factory(private val repository: IFitRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackerViewModel(repository) as T
        }
    }
}

