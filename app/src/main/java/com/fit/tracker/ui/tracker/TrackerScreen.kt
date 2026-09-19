package com.fit.tracker.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fit.tracker.ui.components.CalorieBurnCard
import com.fit.tracker.ui.components.CalorieIntakeCard
import com.fit.tracker.ui.components.ExerciseSection
import com.fit.tracker.ui.components.FitTopAppBar
import com.fit.tracker.ui.components.FoodIntakeDialog
import com.fit.tracker.ui.components.dialogs.CalorieBreakdownDialog
import com.fit.tracker.ui.components.dialogs.MetabolicProfileDialog
import com.fit.tracker.ui.components.sheet.EditExercisePlanBottomSheet

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier,
    initialOpenFoodDialog: Boolean = false,
    onFoodDialogOpened: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val allFoods by viewModel.allFoods.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showFoodDialog by remember { mutableStateOf(false) }
    var showBreakdownDialog by remember { mutableStateOf(false) }
    var showEditPlanDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(initialOpenFoodDialog) {
        if (initialOpenFoodDialog) {
            showFoodDialog = true
            onFoodDialogOpened()
        }
    }

    Scaffold(
        topBar = {
            FitTopAppBar(
                currentDate = uiState.date,
                onDateSelected = { viewModel.selectDate(it) },
                onProfileClick = { showProfileDialog = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 今日饮食摄入看板 (大号指标 + 记饮食按钮 + 可折叠流水明细)
            CalorieIntakeCard(
                totalIntakeKcal = uiState.totalIntakeKcal,
                foodCount = uiState.foodEntries.size,
                foodEntries = uiState.foodEntries,
                onDeleteFoodRecord = { viewModel.deleteFoodRecord(it) },
                onCalculateClick = { showFoodDialog = true }
            )

            // 2. 今日运动目标与进度看板 (内嵌波浪进度条 + 动态消耗)
            CalorieBurnCard(
                targetBurnKcal = uiState.targetBurnKcal,
                completedBurnKcal = uiState.completedBurnKcal,
                completionRatio = uiState.completionRatio,
                isCalculating = isCalculating,
                onCardClick = { showBreakdownDialog = true }
            )

            // 3. 精致运动打卡卡片列表 (无饮食时显示待命引导卡)
            ExerciseSection(
                exercises = uiState.activeExercises,
                onToggleCompleted = { viewModel.toggleExerciseCompleted(it) },
                onEditPlanClick = { showEditPlanDialog = true },
                onAddFoodClick = { showFoodDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 弹窗层
    if (showFoodDialog) {
        FoodIntakeDialog(
            allFoods = allFoods,
            onDismiss = { showFoodDialog = false },
            onConfirm = { food, grams ->
                viewModel.addFoodRecord(food, grams)
                showFoodDialog = false
            },
            onQuickConfirm = { name, calories, carbs, protein, fat ->
                viewModel.addQuickFoodRecord(name, calories, carbs, protein, fat)
                showFoodDialog = false
            }
        )
    }

    if (showBreakdownDialog) {
        CalorieBreakdownDialog(
            totalIntakeKcal = uiState.totalIntakeKcal,
            targetBurnKcal = uiState.targetBurnKcal,
            completedBurnKcal = uiState.completedBurnKcal,
            bmrKcal = uiState.bmrKcal,
            tdeeKcal = uiState.tdeeKcal,
            onDismiss = { showBreakdownDialog = false }
        )
    }

    if (showEditPlanDialog) {
        EditExercisePlanBottomSheet(
            exercises = uiState.activeExercises,
            availableExercises = viewModel.availableExercises,
            onAdjustDelta = { id, delta -> viewModel.adjustExerciseDelta(id, delta) },
            onSetUnits = { id, units -> viewModel.setExerciseUnits(id, units) },
            onRemoveExercise = { id -> viewModel.removeExercise(id) },
            onAddExercise = { exercise -> viewModel.addExercise(exercise) },
            onDismiss = { showEditPlanDialog = false }
        )
    }

    if (showProfileDialog) {
        MetabolicProfileDialog(
            profile = uiState.userProfile,
            bmrKcal = uiState.bmrKcal,
            tdeeKcal = uiState.tdeeKcal,
            onSaveProfile = { newProfile ->
                viewModel.saveUserProfile(newProfile)
            },
            onDismiss = { showProfileDialog = false }
        )
    }
}
