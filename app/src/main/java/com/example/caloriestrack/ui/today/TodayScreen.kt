package com.example.caloriestrack.ui.today

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.caloriestrack.data.GoalEntity
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
    var goals by remember { mutableStateOf<GoalEntity?>(null) }
    var editingEntry by remember { mutableStateOf<FoodEntryEntity?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isSelectingProduct by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun clearFoodForm() {
        editingEntry = null
        selectedProduct = null
        amount = ""
        errorMessage = null
    }

    LaunchedEffect(repository) {
        repository.observeProducts().collect { products = it }
    }

    LaunchedEffect(repository, dateText) {
        repository.observeEntriesForDate(dateText).collect { entries = it }
    }

    LaunchedEffect(repository) {
        repository.observeGoals().collect { goals = it }
    }

    if (isSelectingProduct) {
        ProductPickerScreen(
            products = products,
            modifier = modifier,
            onBack = { isSelectingProduct = false },
            onFavoriteToggle = { product ->
                coroutineScope.launch {
                    repository.setProductFavorite(product.id, !product.isFavorite)
                }
            },
            onProductSelected = { product ->
                selectedProduct = product
                amount = product.defaultEntryAmount()
                errorMessage = null
                isSelectingProduct = false
            }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TodaySummary(
                date = date,
                entries = entries,
                goals = goals
            )
        }

        item {
            AddEditFoodSection(
                editingEntry = editingEntry,
                products = products,
                selectedProduct = selectedProduct,
                amount = amount,
                errorMessage = errorMessage,
                onSelectProductClick = { isSelectingProduct = true },
                onAmountChange = {
                    amount = it
                    errorMessage = null
                },
                onUsePortionFraction = { fraction ->
                    selectedProduct?.let { product ->
                        amount = product.portionFractionAmount(fraction)
                        errorMessage = null
                    }
                },
                onAddFood = {
                    val entry = buildEntryOrNull(
                        date = dateText,
                        product = selectedProduct,
                        amount = amount,
                        existingEntry = editingEntry
                    )

                    if (entry == null) {
                        errorMessage = "Select a product and enter a valid amount."
                        return@AddEditFoodSection
                    }

                    coroutineScope.launch {
                        if (editingEntry == null) {
                            repository.addFoodEntry(entry)
                        } else {
                            repository.updateFoodEntry(entry)
                        }
                        clearFoodForm()
                    }
                },
                onCancelEdit = ::clearFoodForm
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
                    onEdit = {
                        val product = products.firstOrNull { it.id == entry.productId }
                        if (product == null) {
                            errorMessage = "This entry cannot be edited because its product was deleted."
                            return@FoodEntryItem
                        }
                        editingEntry = entry
                        selectedProduct = product
                        amount = entry.toEditableAmount(product)
                        errorMessage = null
                    },
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteFoodEntry(entry)
                            if (editingEntry?.id == entry.id) {
                                clearFoodForm()
                            }
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
    entries: List<FoodEntryEntity>,
    goals: GoalEntity?
) {
    val calories = entries.sumOf { it.calories }
    val protein = entries.sumOf { it.proteinGrams }
    val carbohydrates = entries.sumOf { it.carbohydrateGrams }
    val fat = entries.sumOf { it.fatGrams }
    val dailyGoal = goals?.dailyCalorieGoal?.takeIf { it > 0 }
    val remainingCalories = dailyGoal?.minus(calories)
    val progress = dailyGoal?.let { (calories / it).toFloat().coerceIn(0f, 1f) }

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
                if (dailyGoal != null && remainingCalories != null && progress != null) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (remainingCalories >= 0) {
                            "${remainingCalories.toCleanText()} kcal remaining"
                        } else {
                            "${(-remainingCalories).toCleanText()} kcal over goal"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "No daily goal set yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
private fun AddEditFoodSection(
    editingEntry: FoodEntryEntity?,
    products: List<ProductEntity>,
    selectedProduct: ProductEntity?,
    amount: String,
    errorMessage: String?,
    onSelectProductClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onUsePortionFraction: (Double) -> Unit,
    onAddFood: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (editingEntry == null) "Add food" else "Edit food",
            style = MaterialTheme.typography.titleLarge
        )
        if (products.isEmpty()) {
            Text(
                text = "Add products in the Products tab first.",
                style = MaterialTheme.typography.bodyLarge
            )
            return
        }
        OutlinedButton(
            onClick = onSelectProductClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedProduct?.name ?: "Select product")
        }
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = selectedProduct?.amountLabel() ?: "Amount"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        if (selectedProduct != null && !selectedProduct.usesPortionInput()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onUsePortionFraction(1.0) }) {
                    Text("Full")
                }
                OutlinedButton(onClick = { onUsePortionFraction(0.5) }) {
                    Text("Half")
                }
                OutlinedButton(onClick = { onUsePortionFraction(0.25) }) {
                    Text("Quarter")
                }
            }
        }
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onAddFood) {
                Text(if (editingEntry == null) "Add to today" else "Update entry")
            }
            if (editingEntry != null) {
                OutlinedButton(onClick = onCancelEdit) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun ProductPickerScreen(
    products: List<ProductEntity>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onFavoriteToggle: (ProductEntity) -> Unit,
    onProductSelected: (ProductEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredProducts = remember(products, query) {
        products.filter { product ->
            product.name.contains(query.trim(), ignoreCase = true) ||
                product.brand.orEmpty().contains(query.trim(), ignoreCase = true) ||
                product.category.orEmpty().contains(query.trim(), ignoreCase = true)
        }.sortedWith(
            compareByDescending<ProductEntity> { it.isFavorite }
                .thenBy { it.name.lowercase() }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
            }
        }
        item {
            Text(
                text = "Select product",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search products") },
                singleLine = true
            )
        }
        if (filteredProducts.isEmpty()) {
            item {
                Text(
                    text = "No products found.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(filteredProducts, key = { it.id }) { product ->
                ProductPickerItem(
                    product = product,
                    onFavoriteToggle = { onFavoriteToggle(product) },
                    onClick = { onProductSelected(product) }
                )
            }
        }
    }
}

@Composable
private fun ProductPickerItem(
    product: ProductEntity,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onFavoriteToggle) {
                    Text(if (product.isFavorite) "*" else "Star")
                }
            }
            if (product.brand.orEmpty().isNotBlank()) {
                Text(
                    text = product.brand.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = product.category.orEmpty().ifBlank { "Other" },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${product.basePortionAmount.toCleanText()} ${product.basePortionUnit} - ${product.calories.toCleanText()} kcal",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "P ${product.proteinGrams.toCleanText()}g  C ${product.carbohydrateGrams.toCleanText()}g  F ${product.fatGrams.toCleanText()}g",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FoodEntryItem(
    entry: FoodEntryEntity,
    onEdit: () -> Unit,
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
            if (entry.productBrand.orEmpty().isNotBlank()) {
                Text(
                    text = entry.productBrand.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "${entry.amount.toCleanText()} ${entry.unit}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "P ${entry.proteinGrams.toCleanText()}g  C ${entry.carbohydrateGrams.toCleanText()}g  F ${entry.fatGrams.toCleanText()}g",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

private fun buildEntryOrNull(
    date: String,
    product: ProductEntity?,
    amount: String,
    existingEntry: FoodEntryEntity? = null
): FoodEntryEntity? {
    val parsedAmount = amount.toDoubleOrNull()
    if (product == null || parsedAmount == null || parsedAmount <= 0 || product.basePortionAmount <= 0) {
        return null
    }

    val usesPortionInput = product.usesPortionInput()
    val multiplier = if (usesPortionInput) {
        parsedAmount
    } else {
        parsedAmount / product.basePortionAmount
    }

    return FoodEntryEntity(
        existingEntry?.id ?: 0,
        date,
        product.id,
        product.name,
        product.brand.orEmpty(),
        parsedAmount,
        if (usesPortionInput) product.servingEntryUnit() else product.basePortionUnit,
        product.calories * multiplier,
        product.proteinGrams * multiplier,
        product.carbohydrateGrams * multiplier,
        product.fatGrams * multiplier,
        existingEntry?.createdAtMillis ?: System.currentTimeMillis()
    )
}

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}

private fun ProductEntity.defaultEntryAmount(): String {
    return if (usesPortionInput()) {
        "1"
    } else {
        basePortionAmount.toCleanText()
    }
}

private fun ProductEntity.portionFractionAmount(fraction: Double): String {
    return if (usesPortionInput()) {
        fraction.toCleanText()
    } else {
        (basePortionAmount * fraction).toCleanText()
    }
}

private fun ProductEntity.amountLabel(): String {
    return if (usesPortionInput()) {
        "Servings ($basePortionUnit)"
    } else {
        "Amount ($basePortionUnit)"
    }
}

private fun ProductEntity.usesPortionInput(): Boolean {
    val normalizedUnit = basePortionUnit.trim().lowercase()
    return normalizedUnit in setOf(
        "per bottle",
        "per package"
    )
}

private fun ProductEntity.servingEntryUnit(): String {
    return "serving $basePortionUnit"
}

private fun FoodEntryEntity.toEditableAmount(product: ProductEntity): String {
    return if (product.usesPortionInput() && unit != product.servingEntryUnit()) {
        (amount / product.basePortionAmount).toCleanText()
    } else {
        amount.toCleanText()
    }
}
