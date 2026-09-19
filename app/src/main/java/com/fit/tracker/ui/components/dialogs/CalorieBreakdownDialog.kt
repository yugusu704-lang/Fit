package com.fit.tracker.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 卡片二触发的热量收支透视对话框 (UI.txt & Grilling 决策)
 */
@Composable
fun CalorieBreakdownDialog(
    totalIntakeKcal: Int,
    targetBurnKcal: Int,
    completedBurnKcal: Int,
    bmrKcal: Int,
    tdeeKcal: Int,
    onDismiss: () -> Unit
) {
    val netBalance = totalIntakeKcal - (tdeeKcal + completedBurnKcal)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日热量收支透视",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ProfileStatRow(label = "饮食摄入热量", value = "+$totalIntakeKcal kcal", isTabular = true)
                ProfileStatRow(label = "TDEE 日常基础支出", value = "-$tdeeKcal kcal", isTabular = true)
                ProfileStatRow(label = "今日运动建议目标", value = "$targetBurnKcal kcal", isTabular = true)
                ProfileStatRow(label = "今日已完成运动消耗", value = "-$completedBurnKcal kcal", isTabular = true)

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                ProfileStatRow(
                    label = "当前净热量差",
                    value = if (netBalance <= 0) "$netBalance kcal (赤字达标)" else "+$netBalance kcal (超出预算)",
                    isTabular = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("确定")
                }
            }
        }
    }
}
