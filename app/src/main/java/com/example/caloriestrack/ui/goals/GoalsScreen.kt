package com.example.caloriestrack.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.caloriestrack.data.CaloriesRepository
import com.example.caloriestrack.data.FoodEntryEntity
import com.example.caloriestrack.data.GoalEntity
import com.example.caloriestrack.data.WeightLogEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val weekStart = remember(today) {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val weekEnd = remember(weekStart) { weekStart.plusDays(6) }
    var goals by remember { mutableStateOf<GoalEntity?>(null) }
    var weekEntries by remember { mutableStateOf(emptyList<FoodEntryEntity>()) }
    var weightLogs by remember { mutableStateOf(emptyList<WeightLogEntity>()) }
    var dailyGoal by remember { mutableStateOf("") }
    var weeklyGoal by remember { mutableStateOf("") }
    var proteinGoal by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var proteinMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val currentWeight = remember(weightLogs) {
        weightLogs.maxByOrNull { it.createdAtMillis }
    }

    LaunchedEffect(repository) {
        repository.observeGoals().collect { savedGoals ->
            goals = savedGoals
            dailyGoal = savedGoals?.dailyCalorieGoal?.takeIf { it > 0 }?.toCleanText() ?: ""
            weeklyGoal = savedGoals?.weeklyCalorieGoal?.takeIf { it > 0 }?.toCleanText() ?: ""
            proteinGoal = savedGoals?.dailyProteinGoal?.takeIf { it > 0 }?.toCleanText() ?: ""
        }
    }

    LaunchedEffect(repository, weekStart, weekEnd) {
        repository.observeEntriesBetweenDates(
            weekStart.toString(),
            weekEnd.toString()
        ).collect { entries ->
            weekEntries = entries
        }
    }

    LaunchedEffect(repository) {
        repository.observeWeightLogs().collect { logs ->
            weightLogs = logs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Goals",
            style = MaterialTheme.typography.headlineMedium
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Daily calories",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = dailyGoal,
                    onValueChange = { input ->
                        dailyGoal = input
                        weeklyGoal = input.toDoubleOrNull()
                            ?.takeIf { it > 0 }
                            ?.let { (it * 7.0).toCleanText() }
                            ?: ""
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Daily calorie goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = weeklyGoal,
                    onValueChange = { input ->
                        weeklyGoal = input
                        dailyGoal = input.toDoubleOrNull()
                            ?.takeIf { it > 0 }
                            ?.let { (it / 7.0).toCleanText() }
                            ?: ""
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weekly calorie goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Text(
                    text = "Daily protein",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = proteinGoal,
                    onValueChange = { input ->
                        proteinGoal = input
                        errorMessage = null
                        successMessage = null
                        proteinMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Daily protein goal (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val latestWeight = currentWeight
                        if (latestWeight == null) {
                            proteinMessage = "No body weight provided. Add your weight in the Weight tab first."
                            errorMessage = null
                            successMessage = null
                            return@Button
                        }

                        proteinGoal = (latestWeight.weightKg * ProteinPerKg).toCleanText()
                        proteinMessage = "Calculated from ${latestWeight.weightKg.toCleanText()} kg x ${ProteinPerKg.toCleanText()}g."
                        errorMessage = null
                        successMessage = null
                    }
                ) {
                    Text("Calculate from current weight")
                }
                if (proteinMessage != null) {
                    Text(
                        text = proteinMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (successMessage != null) {
                    Text(
                        text = successMessage ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = {
                        val parsedDailyGoal = dailyGoal.toDoubleOrNull()
                        val parsedWeeklyGoal = weeklyGoal.toDoubleOrNull()
                        val parsedProteinGoal = proteinGoal.toDoubleOrNull()
                        if (parsedDailyGoal == null || parsedDailyGoal <= 0) {
                            errorMessage = "Enter a daily calorie goal greater than 0."
                            successMessage = null
                            return@Button
                        }
                        if (parsedWeeklyGoal == null || parsedWeeklyGoal <= 0) {
                            errorMessage = "Enter a weekly calorie goal greater than 0."
                            successMessage = null
                            return@Button
                        }
                        if (proteinGoal.isNotBlank() && (parsedProteinGoal == null || parsedProteinGoal <= 0)) {
                            errorMessage = "Enter a protein goal greater than 0, or leave it empty."
                            successMessage = null
                            return@Button
                        }

                        val updatedGoals = GoalEntity(
                            GoalEntity.DEFAULT_GOALS_ID,
                            parsedDailyGoal,
                            parsedWeeklyGoal,
                            parsedProteinGoal ?: 0.0,
                            System.currentTimeMillis()
                        )

                        coroutineScope.launch {
                            repository.saveGoals(updatedGoals)
                            successMessage = "Goals saved."
                            errorMessage = null
                        }
                    }
                ) {
                    Text("Save goals")
                }
            }
        }
        WeeklyGoalSummary(
            weekStart = weekStart,
            weekEnd = weekEnd,
            weeklyGoal = goals?.weeklyCalorieGoal?.takeIf { it > 0 },
            entries = weekEntries,
            today = today
        )
    }
}

@Composable
private fun WeeklyGoalSummary(
    weekStart: LocalDate,
    weekEnd: LocalDate,
    weeklyGoal: Double?,
    entries: List<FoodEntryEntity>,
    today: LocalDate
) {
    val consumed = entries.sumOf { it.calories }
    val remaining = weeklyGoal?.minus(consumed)
    val daysRemaining = (weekEnd.toEpochDay() - today.toEpochDay() + 1)
        .coerceAtLeast(1)
        .toDouble()
    val averageRemaining = remaining?.div(daysRemaining)
    val progress = weeklyGoal?.let { (consumed / it).toFloat().coerceIn(0f, 1f) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${weekStart} to ${weekEnd}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (weeklyGoal == null || remaining == null || averageRemaining == null || progress == null) {
                Text(
                    text = "Set a weekly goal to track Monday-Sunday progress.",
                    style = MaterialTheme.typography.bodyLarge
                )
                return@Column
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            GoalMetricRow(
                label = "Weekly goal",
                value = "${weeklyGoal.toCleanText()} kcal"
            )
            GoalMetricRow(
                label = "Consumed so far",
                value = "${consumed.toCleanText()} kcal"
            )
            GoalMetricRow(
                label = if (remaining >= 0) "Remaining" else "Over goal",
                value = "${kotlin.math.abs(remaining).toCleanText()} kcal"
            )
            GoalMetricRow(
                label = "Average remaining per day",
                value = if (remaining >= 0) {
                    "${averageRemaining.toCleanText()} kcal"
                } else {
                    "0 kcal"
                }
            )
        }
    }
}

@Composable
private fun GoalMetricRow(
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

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}

private const val ProteinPerKg = 1.7
