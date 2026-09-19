package com.fit.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit.tracker.domain.model.FoodItem
import com.fit.tracker.ui.theme.TabularMetricStyle
import kotlin.math.roundToInt

/**
 * 饮食录入底部抽屉：
 * 双 Tab 架构：
 * 1. 【食物库称重】：原有食物搜索、分类与称重克数录入；
 * 2. 【热量计算器 / 极简速记】：直接输入大卡 (1秒速记) + 三大营养素 (4C+4P+9F) 速算器。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodIntakeDialog(
    allFoods: List<FoodItem>,
    onDismiss: () -> Unit,
    onConfirm: (FoodItem, Double) -> Unit,
    onQuickConfirm: (name: String, calories: Int, carbs: Double, protein: Double, fat: Double) -> Unit = { _, _, _, _, _ -> }
) {
    val haptic = LocalHapticFeedback.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // --- Tab 1: 食物库状态 ---
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("全部") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(allFoods.firstOrNull()) }
    var gramsText by remember { mutableStateOf("150") }

    val categories = listOf("全部", "主食谷物", "肉蛋蛋白", "蔬菜纤维", "水果坚果")

    val filteredFoods = allFoods.filter { food ->
        val matchesQuery = food.name.contains(searchQuery.trim(), ignoreCase = true) ||
                food.pinyin.contains(searchQuery.trim(), ignoreCase = true)
        val matchesCategory = if (selectedCategory == "全部") {
            true
        } else {
            food.category == selectedCategory
        }
        matchesQuery && matchesCategory
    }

    val grams = gramsText.toDoubleOrNull() ?: 0.0
    val liveCalories = selectedFood?.let {
        ((it.caloriesPer100g * grams) / 100.0).roundToInt()
    } ?: 0

    // --- Tab 2: 热量计算器状态 ---
    var directKcalText by remember { mutableStateOf("450") }
    var directNameText by remember { mutableStateOf("外卖简餐") }

    var carbsText by remember { mutableStateOf("50") }
    var proteinText by remember { mutableStateOf("25") }
    var fatText by remember { mutableStateOf("12") }

    val carbsVal = carbsText.toDoubleOrNull() ?: 0.0
    val proteinVal = proteinText.toDoubleOrNull() ?: 0.0
    val fatVal = fatText.toDoubleOrNull() ?: 0.0
    val macroCalories = ((4.0 * carbsVal) + (4.0 * proteinVal) + (9.0 * fatVal)).roundToInt()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            // 顶部标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (selectedTabIndex == 0) Icons.Default.Restaurant else Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTabIndex == 0) "记录今日饮食" else "内置热量计算器",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 双模式 Tab 切换
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTabIndex = 0
                    },
                    text = { Text("食物库称重", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTabIndex = 1
                    },
                    text = { Text("热量计算器 / 速记", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTabIndex == 0) {
                // ==================== TAB 0: 食物库精算 ====================
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索食物名称或拼音...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = (selectedCategory == cat),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredFoods) { food ->
                        val isSelected = (selectedFood?.id == food.id)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedFood = food
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = food.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = food.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${food.caloriesPer100g.toInt()} kcal / 100g",
                                    style = MaterialTheme.typography.bodySmall.merge(TabularMetricStyle),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = gramsText,
                        onValueChange = { gramsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("摄入克重 (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "合计热量",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "$liveCalories kcal",
                            style = MaterialTheme.typography.titleLarge.merge(TabularMetricStyle),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            selectedFood?.let { food ->
                                if (grams > 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onConfirm(food, grams)
                                }
                            }
                        },
                        enabled = (selectedFood != null && grams > 0),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1.6f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("保存并重平衡计划", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // ==================== TAB 1: 热量计算器 / 极简速记 ====================
                // 1. 极简直接输大卡 (1秒速记)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "方式一：直接输大卡 (1秒速记)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "吃外卖、现成轻食或包装食品时，直接输入总热量",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = directNameText,
                                onValueChange = { directNameText = it },
                                label = { Text("名称/备注") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = directKcalText,
                                onValueChange = { directKcalText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("热量 (kcal)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val directKcalVal = directKcalText.toIntOrNull() ?: 0
                        Button(
                            onClick = {
                                if (directKcalVal > 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onQuickConfirm(directNameText.ifBlank { "快捷饮食" }, directKcalVal, 0.0, 0.0, 0.0)
                                    onDismiss()
                                }
                            },
                            enabled = directKcalVal > 0,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("1 秒直接记入今日摄入", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. 三大营养素速算器 (Macro Calculator)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "方式二：三大营养素速算器",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "公式：碳水×4 + 蛋白质×4 + 脂肪×9 = 总热量",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = carbsText,
                                onValueChange = { carbsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("碳水(g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = proteinText,
                                onValueChange = { proteinText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("蛋白(g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = fatText,
                                onValueChange = { fatText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("脂肪(g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 换算公式结果看板
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "测算合计：$macroCalories kcal",
                                    style = MaterialTheme.typography.titleMedium.merge(TabularMetricStyle),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "(${carbsVal.toInt()}C · ${proteinVal.toInt()}P · ${fatVal.toInt()}F)",
                                    style = MaterialTheme.typography.bodySmall.merge(TabularMetricStyle),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (macroCalories > 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val name = "营养配餐(${carbsVal.toInt()}C/${proteinVal.toInt()}P/${fatVal.toInt()}F)"
                                    onQuickConfirm(name, macroCalories, carbsVal, proteinVal, fatVal)
                                    onDismiss()
                                }
                            },
                            enabled = macroCalories > 0,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("按测算结果记入今日", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
