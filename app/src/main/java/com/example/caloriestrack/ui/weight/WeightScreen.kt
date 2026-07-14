package com.example.caloriestrack.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.caloriestrack.data.WeightLogEntity
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun WeightScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now().toString() }
    var date by remember { mutableStateOf(today) }
    var weight by remember { mutableStateOf("") }
    var weightLogs by remember { mutableStateOf(emptyList<WeightLogEntity>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(repository) {
        repository.observeWeightLogs().collect { logs ->
            weightLogs = logs
        }
    }

    val latestWeight = weightLogs.maxByOrNull { it.createdAtMillis }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Weight",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current weight",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = latestWeight?.let { "${it.weightKg.toCleanText()} kg" } ?: "No weight saved yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (latestWeight != null) {
                        Text(
                            text = latestWeight.date,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weight for this day (kg)") },
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
                        val parsedDate = runCatching { LocalDate.parse(date.trim()) }.getOrNull()
                        val parsedWeight = weight.toDoubleOrNull()

                        if (parsedDate == null) {
                            errorMessage = "Enter a valid date in yyyy-mm-dd format."
                            successMessage = null
                            return@Button
                        }
                        if (parsedWeight == null || parsedWeight <= 0) {
                            errorMessage = "Enter a valid weight greater than 0."
                            successMessage = null
                            return@Button
                        }

                        val weightLog = WeightLogEntity(
                            0,
                            parsedDate.toString(),
                            parsedWeight,
                            System.currentTimeMillis()
                        )

                        coroutineScope.launch {
                            repository.saveWeightLog(weightLog)
                            weight = ""
                            successMessage = "Weight saved."
                            errorMessage = null
                        }
                    }
                ) {
                    Text("Add weight")
                }
            }
        }
        item {
            Text(
                text = "Saved weights",
                style = MaterialTheme.typography.titleLarge
            )
        }
        if (weightLogs.isEmpty()) {
            item {
                Text(
                    text = "No weights saved yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(weightLogs, key = { it.id }) { weightLog ->
                WeightLogItem(
                    weightLog = weightLog,
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteWeightLog(weightLog)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun WeightLogItem(
    weightLog: WeightLogEntity,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = weightLog.date,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${weightLog.weightKg.toCleanText()} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}
