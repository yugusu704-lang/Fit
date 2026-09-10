package com.fit.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.ui.theme.SuccessLight
import com.fit.tracker.ui.theme.TabularMetricStyle

@Composable
fun AnchoredBottomSheetTaskDrawer(
    totalIntakeKcal: Int,
    targetBurnKcal: Int,
    completedBurnKcal: Int,
    exercises: List<ExerciseAllocation>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleCompleted: (ExerciseAllocation) -> Unit,
    onEditPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val completedCount = exercises.count { it.isCompleted }
    val totalCount = exercises.size
    val allCompleted = totalCount > 0 && completedCount == totalCount

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "arrowRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cornerRadius)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, cornerRadius)
    ) {
        // Drag Handle & Header Summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggleExpanded() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today's Intake vs Burn summary
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "今日摄入 $totalIntakeKcal kcal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                        Text(
                            text = "目标消耗 $targetBurnKcal kcal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (allCompleted) "🎉 今日运动任务已全部达标！"
                        else "已完成 $completedCount/$totalCount 项 · 实际已消耗 $completedBurnKcal kcal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (allCompleted) SuccessLight else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = if (allCompleted) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                }

                // Expand/Collapse arrow
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand or Collapse",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Bar
            val progress = if (targetBurnKcal > 0) {
                (completedBurnKcal.toFloat() / targetBurnKcal.toFloat()).coerceIn(0f, 1f)
            } else 1f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (allCompleted) SuccessLight else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }

        // Expandable Workout Sub-Card List
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(250)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "待执行运动清单 (点击左圈打卡)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                exercises.forEach { item ->
                    val isDone = item.isCompleted
                    val cardShape = RoundedCornerShape(10.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(cardShape)
                            .background(
                                if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isDone) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outline,
                                cardShape
                            )
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Tactile Check Circle
                            TactileCheckCircle(
                                isCompleted = isDone,
                                onToggle = { onToggleCompleted(item) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Middle: Exercise Title & Units
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.exercise.name} ${item.units} ${item.exercise.unitLabel}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (isDone) "已完成 · 身体能量已消耗"
                                    else "待完成 · 预计耗时 ${(item.calories * 0.15).toInt().coerceAtLeast(5)} 分钟",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 11.sp,
                                        color = if (isDone) SuccessLight.copy(alpha = 0.8f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                    )
                                )
                            }

                            // Right: Calorie Burn
                            Text(
                                text = "+${item.calories} kcal",
                                style = TabularMetricStyle.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDone) SuccessLight else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Re-adjust Plan Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "调整/重新规划运动项",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEditPlan() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
