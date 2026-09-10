package com.fit.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit.tracker.domain.model.ExerciseAllocation
import com.fit.tracker.ui.theme.TabularMetricStyle
import kotlin.math.roundToInt

@Composable
fun ExerciseStepperCard(
    allocation: ExerciseAllocation,
    onAdjustDeltaUnits: (Int) -> Unit,
    onToggleLock: () -> Unit,
    modifier: Modifier = Modifier,
    canAdjust: Boolean = true
) {
    val shape = RoundedCornerShape(12.dp)
    val isLocked = allocation.isLocked

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Exercise Name & Unit Label
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = allocation.exercise.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onToggleLock,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Outlined.LockOpen,
                            contentDescription = if (isLocked) "Locked" else "Unlocked",
                            tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${allocation.calories} kcal · 每${allocation.exercise.stepQuantum}${allocation.exercise.unitLabel}约${(allocation.exercise.stepQuantum * allocation.exercise.kcalPerUnit).roundToInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontSize = 12.sp
                    )
                )
            }

            // Right: Stepper Controls & Tabular Quantity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (canAdjust && !isLocked && allocation.units > allocation.exercise.stepQuantum)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            else Color.Transparent
                        )
                        .clickable(
                            enabled = canAdjust && !isLocked && allocation.units > allocation.exercise.stepQuantum
                        ) {
                            onAdjustDeltaUnits(-allocation.exercise.stepQuantum)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (canAdjust && !isLocked && allocation.units > allocation.exercise.stepQuantum)
                            MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tabular units display (never jitters layout)
                Text(
                    text = "${allocation.units} ${allocation.exercise.unitLabel}",
                    style = TabularMetricStyle.copy(
                        fontSize = 16.sp,
                        color = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Plus button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (canAdjust && !isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            else Color.Transparent
                        )
                        .clickable(enabled = canAdjust && !isLocked) {
                            onAdjustDeltaUnits(allocation.exercise.stepQuantum)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = if (canAdjust && !isLocked) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
