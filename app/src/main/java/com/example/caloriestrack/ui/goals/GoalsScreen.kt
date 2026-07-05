package com.example.caloriestrack.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.example.caloriestrack.data.GoalEntity
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier
) {
    var goals by remember { mutableStateOf<GoalEntity?>(null) }
    var dailyGoal by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(repository) {
        repository.observeGoals().collect { savedGoals ->
            goals = savedGoals
            dailyGoal = savedGoals?.dailyCalorieGoal?.takeIf { it > 0 }?.toCleanText() ?: ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
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
                    onValueChange = {
                        dailyGoal = it
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Daily calorie goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
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
                        if (parsedDailyGoal == null || parsedDailyGoal <= 0) {
                            errorMessage = "Enter a daily calorie goal greater than 0."
                            successMessage = null
                            return@Button
                        }

                        val updatedGoals = GoalEntity(
                            GoalEntity.DEFAULT_GOALS_ID,
                            parsedDailyGoal,
                            goals?.weeklyCalorieGoal ?: 0.0,
                            System.currentTimeMillis()
                        )

                        coroutineScope.launch {
                            repository.saveGoals(updatedGoals)
                            successMessage = "Daily goal saved."
                            errorMessage = null
                        }
                    }
                ) {
                    Text("Save daily goal")
                }
            }
        }
        Text(
            text = "Weekly goals will be added in a later step.",
            style = MaterialTheme.typography.bodyLarge
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
