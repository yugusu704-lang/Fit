package com.fit.tracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fit.tracker.ui.theme.TabularMetricStyle

/**
 * 今日运动目标与进度看板：
 * 动态波浪圆端进度条、等宽防抖收支差值微仪表、收支透视弹窗入口。
 */
@Composable
fun CalorieBurnCard(
    targetBurnKcal: Int,
    completedBurnKcal: Int,
    completionRatio: Float,
    isCalculating: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val animatedProgress by animateFloatAsState(
        targetValue = completionRatio.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "WorkoutProgress"
    )

    val remainingKcal = (targetBurnKcal - completedBurnKcal).coerceAtLeast(0)
    val isGoalReached = completedBurnKcal >= targetBurnKcal && targetBurnKcal > 0

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCardClick()
            })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 头部标题与百分比
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "今日运动目标与进度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = if (targetBurnKcal <= 0) "待测算" else "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.merge(TabularMetricStyle),
                    fontWeight = FontWeight.Bold,
                    color = if (targetBurnKcal <= 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 消耗热量大字指标
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$completedBurnKcal",
                    style = MaterialTheme.typography.headlineMedium.merge(TabularMetricStyle),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (targetBurnKcal <= 0) " / 待生成 kcal" else " / $targetBurnKcal kcal",
                    style = MaterialTheme.typography.bodyLarge.merge(TabularMetricStyle),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 融为一体的圆端线性进度条
            if (isCalculating || targetBurnKcal <= 0) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (targetBurnKcal <= 0) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.secondaryContainer,
                    strokeCap = StrokeCap.Round
                )
            } else {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.secondaryContainer,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 微仪表：直观显示“已消耗 X kcal · 距离达标剩余 Y kcal / 已达成 / 待生成”
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (targetBurnKcal <= 0) {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    } else if (isGoalReached) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                    }
                ) {
                    Text(
                        text = if (targetBurnKcal <= 0) {
                            "待生成 · 录入第一餐后自动测算"
                        } else if (isGoalReached) {
                            if (completedBurnKcal > targetBurnKcal) {
                                "已达标 · 超额消耗 ${completedBurnKcal - targetBurnKcal} kcal"
                            } else {
                                "今日目标已达成 ✓"
                            }
                        } else {
                            "已消耗 $completedBurnKcal kcal · 距离达标剩余 $remainingKcal kcal"
                        },
                        style = MaterialTheme.typography.labelSmall.merge(TabularMetricStyle),
                        color = if (targetBurnKcal <= 0) {
                            MaterialTheme.colorScheme.outline
                        } else if (isGoalReached) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 底部透视入口提示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "点击查看今日热量收支透视",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
