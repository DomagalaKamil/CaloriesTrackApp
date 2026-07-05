package com.example.caloriestrack.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.caloriestrack.data.FoodEntryEntity
import com.example.caloriestrack.data.ProductEntity
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier,
    date: LocalDate = LocalDate.now()
) {
    val dateText = remember(date) { date.toString() }
    var products by remember { mutableStateOf(emptyList<ProductEntity>()) }
    var entries by remember { mutableStateOf(emptyList<FoodEntryEntity>()) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(repository) {
        repository.observeProducts().collect { products = it }
    }

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
            TodaySummary(date = date, entries = entries)
        }

        item {
            AddFoodSection(
                products = products,
                selectedProduct = selectedProduct,
                amount = amount,
                errorMessage = errorMessage,
                onProductSelected = {
                    selectedProduct = it
                    amount = it.basePortionAmount.toCleanText()
                    errorMessage = null
                },
                onAmountChange = {
                    amount = it
                    errorMessage = null
                },
                onAddFood = {
                    val entry = buildEntryOrNull(
                        date = dateText,
                        product = selectedProduct,
                        amount = amount
                    )

                    if (entry == null) {
                        errorMessage = "Select a product and enter a valid amount."
                        return@AddFoodSection
                    }

                    coroutineScope.launch {
                        repository.addFoodEntry(entry)
                        selectedProduct = null
                        amount = ""
                        errorMessage = null
                    }
                }
            )
        }

        item {
            Text(
                text = "Today's food",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (entries.isEmpty()) {
            item {
                Text(
                    text = "No food added today.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                FoodEntryItem(
                    entry = entry,
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteFoodEntry(entry)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TodaySummary(
    date: LocalDate,
    entries: List<FoodEntryEntity>
) {
    val calories = entries.sumOf { it.calories }
    val protein = entries.sumOf { it.proteinGrams }
    val carbohydrates = entries.sumOf { it.carbohydrateGrams }
    val fat = entries.sumOf { it.fatGrams }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = date.toString(),
            style = MaterialTheme.typography.bodyLarge
        )
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
}

@Composable
private fun AddFoodSection(
    products: List<ProductEntity>,
    selectedProduct: ProductEntity?,
    amount: String,
    errorMessage: String?,
    onProductSelected: (ProductEntity) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddFood: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Add food",
            style = MaterialTheme.typography.titleLarge
        )
        if (products.isEmpty()) {
            Text(
                text = "Add products in the Products tab first.",
                style = MaterialTheme.typography.bodyLarge
            )
            return
        }
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedProduct?.name ?: "Select product")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                products.forEach { product ->
                    DropdownMenuItem(
                        text = { Text(product.name) },
                        onClick = {
                            onProductSelected(product)
                            expanded = false
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = selectedProduct?.let { "Amount (${it.basePortionUnit})" } ?: "Amount"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        selectedProduct?.let { product ->
            val previewEntry = buildEntryOrNull(
                date = LocalDate.now().toString(),
                product = product,
                amount = amount
            )
            if (previewEntry != null) {
                Text(
                    text = "${previewEntry.calories.toCleanText()} kcal - P ${previewEntry.proteinGrams.toCleanText()}g  C ${previewEntry.carbohydrateGrams.toCleanText()}g  F ${previewEntry.fatGrams.toCleanText()}g",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(onClick = onAddFood) {
            Text("Add to today")
        }
    }
}

@Composable
private fun FoodEntryItem(
    entry: FoodEntryEntity,
    onDelete: () -> Unit
) {
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
            Text(
                text = "${entry.amount.toCleanText()}${entry.unit}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "P ${entry.proteinGrams.toCleanText()}g  C ${entry.carbohydrateGrams.toCleanText()}g  F ${entry.fatGrams.toCleanText()}g",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

private fun buildEntryOrNull(
    date: String,
    product: ProductEntity?,
    amount: String
): FoodEntryEntity? {
    val parsedAmount = amount.toDoubleOrNull()
    if (product == null || parsedAmount == null || parsedAmount <= 0 || product.basePortionAmount <= 0) {
        return null
    }

    val multiplier = parsedAmount / product.basePortionAmount

    return FoodEntryEntity(
        0,
        date,
        product.id,
        product.name,
        parsedAmount,
        product.basePortionUnit,
        product.calories * multiplier,
        product.proteinGrams * multiplier,
        product.carbohydrateGrams * multiplier,
        product.fatGrams * multiplier,
        System.currentTimeMillis()
    )
}

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}
