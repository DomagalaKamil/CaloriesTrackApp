package com.example.caloriestrack.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import java.time.LocalDate

@Composable
fun HistoryScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var entries by remember { mutableStateOf(emptyList<FoodEntryEntity>()) }
    val dateText = remember(selectedDate) { selectedDate.toString() }

    LaunchedEffect(repository, dateText) {
        repository.observeEntriesForDate(dateText).collect { entries = it }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HistoryHeader(
                selectedDate = selectedDate,
                onPreviousDay = { selectedDate = selectedDate.minusDays(1) },
                onNextDay = { selectedDate = selectedDate.plusDays(1) },
                onToday = { selectedDate = LocalDate.now() }
            )
        }

        item {
            DaySummary(entries = entries)
        }

        item {
            Text(
                text = "Food list",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (entries.isEmpty()) {
            item {
                Text(
                    text = "No food entries for this day.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                HistoryEntryItem(entry = entry)
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = selectedDate.toString(),
            style = MaterialTheme.typography.titleLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPreviousDay) {
                Text("Previous")
            }
            OutlinedButton(onClick = onToday) {
                Text("Today")
            }
            OutlinedButton(onClick = onNextDay) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun DaySummary(entries: List<FoodEntryEntity>) {
    val calories = entries.sumOf { it.calories }
    val protein = entries.sumOf { it.proteinGrams }
    val carbohydrates = entries.sumOf { it.carbohydrateGrams }
    val fat = entries.sumOf { it.fatGrams }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${calories.toCleanText()} kcal",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Protein ${protein.toCleanText()}g",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Carbs ${carbohydrates.toCleanText()}g",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Fat ${fat.toCleanText()}g",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HistoryEntryItem(entry: FoodEntryEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.productName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${entry.calories.toCleanText()} kcal",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (entry.productBrand.orEmpty().isNotBlank()) {
                Text(
                    text = entry.productBrand.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "${entry.amount.toCleanText()}${entry.unit}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "P ${entry.proteinGrams.toCleanText()}g  C ${entry.carbohydrateGrams.toCleanText()}g  F ${entry.fatGrams.toCleanText()}g",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}
