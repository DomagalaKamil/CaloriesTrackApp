package com.example.caloriestrack.ui.analysis

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.caloriestrack.data.CaloriesRepository
import com.example.caloriestrack.data.FoodEntryEntity
import com.example.caloriestrack.data.GoalEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun AnalysisScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val currentWeekStart = remember(today) {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    var weekStart by remember { mutableStateOf(currentWeekStart) }
    val weekEnd = remember(weekStart) { weekStart.plusDays(6) }
    val selectableWeeks = remember(currentWeekStart) {
        (-26..26).map { offset -> currentWeekStart.plusWeeks(offset.toLong()) }
    }
    var entries by remember { mutableStateOf(emptyList<FoodEntryEntity>()) }
    var goals by remember { mutableStateOf<GoalEntity?>(null) }

    LaunchedEffect(repository, weekStart, weekEnd) {
        repository.observeEntriesBetweenDates(
            weekStart.toString(),
            weekEnd.toString()
        ).collect { weekEntries ->
            entries = weekEntries
        }
    }

    LaunchedEffect(repository) {
        repository.observeGoals().collect { savedGoals ->
            goals = savedGoals
        }
    }

    val stats = remember(entries, goals, weekStart, weekEnd) {
        WeeklyStats.from(
            entries = entries,
            weeklyGoal = goals?.weeklyCalorieGoal?.takeIf { it > 0 },
            weekStart = weekStart,
            weekEnd = weekEnd
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Analysis",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "${weekStart.toDisplayDate()} to ${weekEnd.toDisplayDate()}",
            style = MaterialTheme.typography.bodyLarge
        )
        WeekSelector(
            weeks = selectableWeeks,
            selectedWeekStart = weekStart,
            onWeekSelected = { selectedWeek -> weekStart = selectedWeek }
        )
        WeeklyOverviewCard(stats = stats)
        MacroTotalsCard(stats = stats)
        DailyBreakdownCard(stats = stats)
    }
}

@Composable
private fun WeekSelector(
    weeks: List<LocalDate>,
    selectedWeekStart: LocalDate,
    onWeekSelected: (LocalDate) -> Unit
) {
    val listState = rememberLazyListState()
    val selectedWeekIndex = remember(weeks, selectedWeekStart) {
        weeks.indexOf(selectedWeekStart).coerceAtLeast(0)
    }

    LaunchedEffect(selectedWeekIndex) {
        val layoutInfo = listState.layoutInfo
        val selectedItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedWeekIndex }
        if (selectedItem != null) {
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val itemCenter = selectedItem.offset + selectedItem.size / 2
            listState.animateScrollBy((itemCenter - viewportCenter).toFloat())
        } else {
            val itemWidth = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
            val viewportWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val centeredOffset = if (itemWidth > 0 && viewportWidth > itemWidth) {
                -((viewportWidth - itemWidth) / 2)
            } else {
                0
            }
            listState.animateScrollToItem(selectedWeekIndex, centeredOffset)
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(weeks) { _, weekStart ->
            val weekEnd = weekStart.plusDays(6)
            val label = "${weekStart.toShortDate()} - ${weekEnd.toShortDate()}"
            if (weekStart == selectedWeekStart) {
                Button(onClick = { onWeekSelected(weekStart) }) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { onWeekSelected(weekStart) }) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun WeeklyOverviewCard(stats: WeeklyStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Weekly calories",
                style = MaterialTheme.typography.titleLarge
            )
            if (stats.weeklyGoal != null) {
                LinearProgressIndicator(
                    progress = { stats.goalProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "This week you consumed ${stats.totalCalories.toCleanText()} / ${stats.weeklyGoal.toCleanText()} kcal.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (stats.goalDifference >= 0) {
                        "You are ${stats.goalDifference.toCleanText()} kcal under your weekly target."
                    } else {
                        "You are ${kotlin.math.abs(stats.goalDifference).toCleanText()} kcal over your weekly target."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Set a weekly goal to compare your intake against a target.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            MetricRow("Daily average", "${stats.averageCalories.toCleanText()} kcal")
            MetricRow("Goal reached", "${stats.goalPercent.toCleanText()}%")
            MetricRow("Highest day", "${stats.highestDay.label}: ${stats.highestDay.calories.toCleanText()} kcal")
            MetricRow("Lowest day", "${stats.lowestDay.label}: ${stats.lowestDay.calories.toCleanText()} kcal")
        }
    }
}

@Composable
private fun MacroTotalsCard(stats: WeeklyStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Weekly macros",
                style = MaterialTheme.typography.titleLarge
            )
            MetricRow("Protein total", "${stats.totalProtein.toCleanText()}g")
            MetricRow("Carbs total", "${stats.totalCarbohydrates.toCleanText()}g")
            MetricRow("Fat total", "${stats.totalFat.toCleanText()}g")
            MetricRow("Average protein per day", "${stats.averageProtein.toCleanText()}g")
        }
    }
}

@Composable
private fun DailyBreakdownCard(stats: WeeklyStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Daily breakdown",
                style = MaterialTheme.typography.titleLarge
            )
            stats.dailyCalories.forEach { day ->
                MetricRow(day.label, "${day.calories.toCleanText()} kcal")
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private data class WeeklyStats(
    val weeklyGoal: Double?,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbohydrates: Double,
    val totalFat: Double,
    val averageCalories: Double,
    val averageProtein: Double,
    val goalDifference: Double,
    val goalPercent: Double,
    val goalProgress: Float,
    val highestDay: DailyCalories,
    val lowestDay: DailyCalories,
    val dailyCalories: List<DailyCalories>
) {
    companion object {
        fun from(
            entries: List<FoodEntryEntity>,
            weeklyGoal: Double?,
            weekStart: LocalDate,
            weekEnd: LocalDate
        ): WeeklyStats {
            val days = generateSequence(weekStart) { current ->
                current.plusDays(1).takeIf { it <= weekEnd }
            }.toList()
            val entriesByDate = entries.groupBy { it.date }
            val dailyCalories = days.map { day ->
                DailyCalories(
                    label = day.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    calories = entriesByDate[day.toString()].orEmpty().sumOf { it.calories }
                )
            }
            val totalCalories = entries.sumOf { it.calories }
            val totalProtein = entries.sumOf { it.proteinGrams }
            val totalCarbohydrates = entries.sumOf { it.carbohydrateGrams }
            val totalFat = entries.sumOf { it.fatGrams }
            val goalPercent = weeklyGoal?.let { (totalCalories / it) * 100 } ?: 0.0

            return WeeklyStats(
                weeklyGoal = weeklyGoal,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbohydrates = totalCarbohydrates,
                totalFat = totalFat,
                averageCalories = totalCalories / 7.0,
                averageProtein = totalProtein / 7.0,
                goalDifference = weeklyGoal?.minus(totalCalories) ?: 0.0,
                goalPercent = goalPercent,
                goalProgress = (goalPercent / 100.0).toFloat().coerceIn(0f, 1f),
                highestDay = dailyCalories.maxByOrNull { it.calories } ?: DailyCalories("Monday", 0.0),
                lowestDay = dailyCalories.minByOrNull { it.calories } ?: DailyCalories("Monday", 0.0),
                dailyCalories = dailyCalories
            )
        }
    }
}

private data class DailyCalories(
    val label: String,
    val calories: Double
)

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}

private fun LocalDate.toShortDate(): String {
    return format(DateTimeFormatter.ofPattern("dd-MM"))
}

private fun LocalDate.toDisplayDate(): String {
    return format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
}
