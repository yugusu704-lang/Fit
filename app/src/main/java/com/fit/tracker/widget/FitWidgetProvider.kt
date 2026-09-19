package com.fit.tracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.fit.tracker.MainActivity
import com.fit.tracker.R
import com.fit.tracker.data.local.db.FitDatabase
import com.fit.tracker.domain.engine.CalorieEngine
import com.fit.tracker.domain.model.ActivityLevel
import com.fit.tracker.domain.model.DefaultCatalogs
import com.fit.tracker.domain.model.Gender
import com.fit.tracker.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

open class FitWidgetProvider(private val isCompact: Boolean) : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id, isCompact)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        FitWidgetRefreshReceiver.scheduleNextMidnightAlarm(context)
    }

    companion object {
        fun notifyWidgetsUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            val ids4x2 = appWidgetManager.getAppWidgetIds(ComponentName(context, FitWidget4x2Provider::class.java))
            if (ids4x2.isNotEmpty()) {
                val intent = Intent(context, FitWidget4x2Provider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4x2)
                }
                context.sendBroadcast(intent)
            }

            val ids4x3 = appWidgetManager.getAppWidgetIds(ComponentName(context, FitWidget4x3Provider::class.java))
            if (ids4x3.isNotEmpty()) {
                val intent = Intent(context, FitWidget4x3Provider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4x3)
                }
                context.sendBroadcast(intent)
            }
        }

        suspend fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            isCompact: Boolean
        ) {
            val today = LocalDate.now().toString()
            val db = FitDatabase.getInstance(context)

            // 1. 读取基础配置与今日饮食
            val profileEntity = db.userProfileDao().getUserProfile().firstOrNull()
            val profile = UserProfile(
                gender = runCatching { Gender.valueOf(profileEntity?.gender ?: "") }.getOrDefault(Gender.MALE),
                age = profileEntity?.age ?: 28,
                heightCm = profileEntity?.heightCm ?: 175.0,
                weightKg = profileEntity?.weightKg ?: 72.0,
                activityLevel = runCatching { ActivityLevel.valueOf(profileEntity?.activityLevel ?: "") }.getOrDefault(ActivityLevel.LIGHT),
                targetDeficitKcal = profileEntity?.targetDeficitKcal ?: 400
            )

            val foodRecords = db.foodRecordDao().getRecordsByDate(today).firstOrNull() ?: emptyList()
            val allFoods = db.foodDao().getAllFoods().firstOrNull() ?: emptyList()
            val foodMap = allFoods.associateBy { it.id }

            val totalIntake = foodRecords.sumOf { record ->
                val food = foodMap[record.foodId]
                if (food != null) {
                    ((record.grams * food.caloriesPer100g) / 100.0).roundToInt()
                } else 0
            }

            // 2. 计算卡路里指标
            val calorieEngine = CalorieEngine()
            val bmr = calorieEngine.calculateBmr(profile)
            val tdee = calorieEngine.calculateTdee(bmr, profile.activityLevel)
            val calculatedBurn = calorieEngine.calculateTargetBurn(totalIntake, tdee, profile.targetDeficitKcal)

            val allocations = db.exerciseAllocationDao().getAllocationsByDate(today).firstOrNull() ?: emptyList()
            val plan = db.dailyPlanDao().getPlanByDate(today).firstOrNull()

            val effectiveTargetBurn = if (foodRecords.isEmpty() && plan == null && allocations.isEmpty()) {
                0
            } else {
                if (calculatedBurn > 0) calculatedBurn else allocations.sumOf { it.calories }
            }

            val completedBurn = allocations.filter { it.isCompleted }.sumOf { it.calories }
            val progressRatio = if (effectiveTargetBurn > 0) {
                ((completedBurn.toDouble() / effectiveTargetBurn) * 100).roundToInt().coerceIn(0, 100)
            } else {
                0
            }

            // 3. 构建 RemoteViews
            val layoutResId = if (isCompact) R.layout.widget_fit_4x2 else R.layout.widget_fit_4x3
            val views = RemoteViews(context.packageName, layoutResId)

            // 卡片整体点击 -> 启动 App 主界面
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPi = PendingIntent.getActivity(
                context,
                widgetId * 10,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPi)

            // "+" 按钮点击 -> 调起快速记饮食弹窗
            val foodIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_OPEN_FOOD_DIALOG)
            }
            val foodPi = PendingIntent.getActivity(
                context,
                widgetId * 10 + 1,
                foodIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_add, foodPi)
            views.setOnClickPendingIntent(R.id.widget_empty_view, foodPi)

            // 标题与日期
            views.setTextViewText(R.id.widget_title, if (isCompact) "FIT" else "FIT 减脂待办")
            val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE))
            views.setTextViewText(R.id.widget_date, dateLabel)

            // 卡路里摘要与进度
            views.setTextViewText(
                R.id.widget_calorie_summary,
                "摄入 $totalIntake · 消耗 $completedBurn/$effectiveTargetBurn kcal"
            )
            views.setProgressBar(R.id.widget_progress, 100, progressRatio, false)
            views.setTextViewText(R.id.widget_progress_ratio, "$progressRatio%")

            // 4. 运动待办列表或空状态
            val isEmpty = foodRecords.isEmpty() && allocations.isEmpty()
            if (isEmpty) {
                views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
                views.setViewVisibility(R.id.widget_exercise_container, View.GONE)
                views.setTextViewText(R.id.widget_footer, "点击右上角 + 记录首餐，激活今日运动处方")
            } else {
                views.setViewVisibility(R.id.widget_empty_view, View.GONE)
                views.setViewVisibility(R.id.widget_exercise_container, View.VISIBLE)
                views.removeAllViews(R.id.widget_exercise_container)

                val maxItems = if (isCompact) 3 else 6
                val visibleAllocations = allocations.take(maxItems)
                val catalogMap = DefaultCatalogs.exercises.associateBy { it.id }

                for (alloc in visibleAllocations) {
                    val def = catalogMap[alloc.exerciseId]
                    val name = def?.name ?: alloc.exerciseId
                    val unitStr = "${alloc.units} ${def?.unitLabel ?: "次"}"
                    val calStr = "${alloc.calories} kcal"

                    val itemView = RemoteViews(context.packageName, R.layout.widget_exercise_item)
                    itemView.setTextViewText(R.id.widget_item_name, name)
                    itemView.setTextViewText(R.id.widget_item_unit, unitStr)
                    itemView.setTextViewText(R.id.widget_item_calories, calStr)

                    if (alloc.isCompleted) {
                        itemView.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_checkbox_checked)
                        itemView.setInt(R.id.widget_item_name, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
                        itemView.setTextColor(R.id.widget_item_name, ContextCompat.getColor(context, R.color.widget_text_tertiary))
                    } else {
                        itemView.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_checkbox_unchecked)
                        itemView.setInt(R.id.widget_item_name, "setPaintFlags", Paint.ANTI_ALIAS_FLAG)
                        itemView.setTextColor(R.id.widget_item_name, ContextCompat.getColor(context, R.color.widget_text_primary))
                    }

                    // 复选框点击 -> 广播打卡
                    val toggleIntent = Intent(context, FitWidgetActionReceiver::class.java).apply {
                        action = FitWidgetActionReceiver.ACTION_TOGGLE_EXERCISE
                        putExtra(FitWidgetActionReceiver.EXTRA_DATE, today)
                        putExtra(FitWidgetActionReceiver.EXTRA_EXERCISE_ID, alloc.exerciseId)
                        data = Uri.parse("fit_toggle://${today}/${alloc.exerciseId}")
                    }
                    val togglePi = PendingIntent.getBroadcast(
                        context,
                        alloc.id.hashCode(),
                        toggleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    itemView.setOnClickPendingIntent(R.id.widget_item_checkbox, togglePi)

                    views.addView(R.id.widget_exercise_container, itemView)
                }

                val completedCount = allocations.count { it.isCompleted }
                val totalCount = allocations.size
                val extraCount = totalCount - visibleAllocations.size
                val footerText = if (extraCount > 0) {
                    "已完成 $completedCount/$totalCount 项 · +$extraCount 更多"
                } else {
                    "已完成 $completedCount/$totalCount 项"
                }
                views.setTextViewText(R.id.widget_footer, footerText)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}

class FitWidget4x2Provider : FitWidgetProvider(isCompact = true)
class FitWidget4x3Provider : FitWidgetProvider(isCompact = false)
