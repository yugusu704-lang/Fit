package com.fit.tracker.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit.tracker.data.repository.FitRepository
import com.fit.tracker.domain.engine.CalorieConservationEngine
import com.fit.tracker.domain.engine.CalorieEngine
import com.fit.tracker.domain.model.DailyTrackerState
import com.fit.tracker.domain.model.DefaultCatalogs
import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.domain.model.ExerciseDefinition
import com.fit.tracker.domain.model.FoodItem
import com.fit.tracker.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class TrackerViewModel(
    private val repository: FitRepository,
    private val calorieEngine: CalorieEngine = CalorieEngine(),
    private val conservationEngine: CalorieConservationEngine = CalorieConservationEngine()
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _userProfile = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    private val _searchQuery = MutableStateFlow("")
    val allFoods: StateFlow<List<FoodItem>> = repository.searchFoods("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultCatalogs.foods)

    // In-memory selection of exercises if plan is not confirmed
    private val _selectedExerciseDefs = MutableStateFlow<Set<String>>(
        setOf("jump_rope", "running_400m")
    )

    private val _inMemoryAllocations = MutableStateFlow<List<ExerciseAllocation>>(emptyList())
    private val _isDrawerExpanded = MutableStateFlow(false)
    val isDrawerExpanded: StateFlow<Boolean> = _isDrawerExpanded.asStateFlow()

    private val _isPlanConfirmed = MutableStateFlow(false)

    val uiState: StateFlow<DailyTrackerState> = combine(
        _currentDate,
        _userProfile,
        _isPlanConfirmed,
        _inMemoryAllocations,
        _isDrawerExpanded
    ) { date, profile, isConfirmed, inMemAllocs, drawerExpanded ->
        buildTrackerState(date, profile, isConfirmed, inMemAllocs, drawerExpanded)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyTrackerState(date = _currentDate.value)
    )

    init {
        // Observe date changes and load records & plans
        viewModelScope.launch {
            _currentDate.collect { date ->
                loadDayData(date)
            }
        }
    }

    private suspend fun loadDayData(date: String) {
        repository.getDailyPlan(date).collect { plan ->
            val isConfirmed = plan?.isConfirmed ?: false
            _isPlanConfirmed.value = isConfirmed
            if (isConfirmed) {
                repository.getAllocations(date).collect { saved ->
                    _inMemoryAllocations.value = saved
                }
            } else {
                recomputeUnconfirmedPlan()
            }
        }
    }

    private fun recomputeUnconfirmedPlan() {
        val profile = _userProfile.value
        val bmr = calorieEngine.calculateBmr(profile)
        val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
        val intake = uiState.value.totalIntakeKcal
        val targetBurn = calorieEngine.calculateTargetBurn(intake, tdee, profile.targetDeficitKcal)

        val selectedCatalog = DefaultCatalogs.exercises.filter {
            _selectedExerciseDefs.value.contains(it.id)
        }
        val allocs = conservationEngine.allocateInitial(selectedCatalog, targetBurn)
        _inMemoryAllocations.value = allocs
    }

    fun selectDate(date: String) {
        _currentDate.value = date
    }

    fun changeDay(delta: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = dateFormat.parse(_currentDate.value) ?: Date()
            cal.time = parsed
            cal.add(Calendar.DAY_OF_YEAR, delta)
            _currentDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            _currentDate.value = dateFormat.format(Date())
        }
    }

    fun toggleExerciseSelection(exercise: ExerciseDefinition) {
        if (_isPlanConfirmed.value) return
        val currentSet = _selectedExerciseDefs.value.toMutableSet()
        if (currentSet.contains(exercise.id)) {
            if (currentSet.size > 1) { // Keep at least one
                currentSet.remove(exercise.id)
            }
        } else {
            currentSet.add(exercise.id)
        }
        _selectedExerciseDefs.value = currentSet
        recomputeUnconfirmedPlan()
    }

    fun isExerciseSelected(exerciseId: String): Boolean {
        return _selectedExerciseDefs.value.contains(exerciseId)
    }

    fun adjustExerciseDelta(exerciseId: String, deltaUnits: Int) {
        val current = _inMemoryAllocations.value
        val target = current.firstOrNull { it.exercise.id == exerciseId } ?: return
        if (target.isLocked || target.isCompleted) return

        val deltaKcal = (deltaUnits * target.exercise.kcalPerUnit).roundToInt()
        val requestedKcal = (target.calories + deltaKcal).coerceAtLeast(0)

        val targetBurn = uiState.value.targetBurnKcal
        val rebalanced = conservationEngine.rebalance(
            allocations = current,
            targetTotalKcal = targetBurn,
            modifiedExerciseId = exerciseId,
            requestedCalories = requestedKcal
        )
        _inMemoryAllocations.value = rebalanced
    }

    fun toggleExerciseLock(exerciseId: String) {
        _inMemoryAllocations.update { list ->
            list.map {
                if (it.exercise.id == exerciseId) it.copy(isLocked = !it.isLocked) else it
            }
        }
    }

    fun confirmPlan() {
        viewModelScope.launch {
            val date = _currentDate.value
            val targetBurn = uiState.value.targetBurnKcal
            val allocs = _inMemoryAllocations.value
            repository.saveDailyPlan(date, targetBurn, isConfirmed = true)
            repository.saveAllocations(date, allocs)
            _isPlanConfirmed.value = true
            _isDrawerExpanded.value = true // Open pull-out card upon confirmation
        }
    }

    fun editPlan() {
        viewModelScope.launch {
            val date = _currentDate.value
            val targetBurn = uiState.value.targetBurnKcal
            repository.saveDailyPlan(date, targetBurn, isConfirmed = false)
            _isPlanConfirmed.value = false
            _isDrawerExpanded.value = false
        }
    }

    fun toggleExerciseCompleted(allocation: ExerciseAllocation) {
        viewModelScope.launch {
            val date = _currentDate.value
            val updated = allocation.copy(isCompleted = !allocation.isCompleted)
            _inMemoryAllocations.update { list ->
                list.map { if (it.exercise.id == allocation.exercise.id) updated else it }
            }
            if (_isPlanConfirmed.value) {
                repository.updateAllocation(date, updated)
            }
        }
    }

    fun toggleDrawer() {
        _isDrawerExpanded.update { !it }
    }

    fun addFoodRecord(food: FoodItem, grams: Double) {
        viewModelScope.launch {
            repository.addFoodRecord(_currentDate.value, food.id, grams)
            // If plan is unconfirmed, auto-rebalance target burn
            if (!_isPlanConfirmed.value) {
                recomputeUnconfirmedPlan()
            }
        }
    }

    fun deleteFoodRecord(recordId: String) {
        viewModelScope.launch {
            repository.deleteFoodRecord(recordId)
            if (!_isPlanConfirmed.value) {
                recomputeUnconfirmedPlan()
            }
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

    private fun buildTrackerState(
        date: String,
        profile: UserProfile,
        isConfirmed: Boolean,
        inMemAllocs: List<ExerciseAllocation>,
        drawerExpanded: Boolean
    ): DailyTrackerState {
        val bmr = calorieEngine.calculateBmr(profile)
        val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
        // Calorie intake from entries
        val intake = uiState.value.foodEntries.sumOf { it.calories }
        val targetBurn = calorieEngine.calculateTargetBurn(intake, tdee, profile.targetDeficitKcal)

        return DailyTrackerState(
            date = date,
            userProfile = profile,
            foodEntries = uiState.value.foodEntries,
            totalIntakeKcal = intake,
            bmrKcal = bmr,
            tdeeKcal = tdee,
            targetBurnKcal = targetBurn,
            activeExercises = inMemAllocs,
            isPlanConfirmed = isConfirmed,
            isDrawerExpanded = drawerExpanded
        )
    }

    class Factory(private val repository: FitRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackerViewModel(repository) as T
        }
    }
}
