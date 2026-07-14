package com.example.caloriestrack.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.caloriestrack.data.ProductEntity
import kotlinx.coroutines.launch

@Composable
fun ProductsScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier
) {
    var products by remember { mutableStateOf(emptyList<ProductEntity>()) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isViewingSavedProducts by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CategoryPlaceholder) }
    var portionAmount by remember { mutableStateOf("100") }
    var portionUnit by remember { mutableStateOf("g") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbohydrates by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun clearForm() {
        editingProduct = null
        name = ""
        brand = ""
        category = CategoryPlaceholder
        portionAmount = "100"
        portionUnit = "g"
        calories = ""
        protein = ""
        carbohydrates = ""
        fat = ""
        errorMessage = null
    }

    fun startEditing(product: ProductEntity) {
        isViewingSavedProducts = false
        editingProduct = product
        name = product.name
        brand = product.brand.orEmpty()
        category = product.category.orEmpty().ifBlank { ProductCategory.Other.label }
        portionAmount = product.basePortionAmount.toCleanText()
        portionUnit = product.basePortionUnit
        val per100Multiplier = 100.0 / product.basePortionAmount
        calories = (product.calories * per100Multiplier).toCleanText()
        protein = (product.proteinGrams * per100Multiplier).toCleanText()
        carbohydrates = (product.carbohydrateGrams * per100Multiplier).toCleanText()
        fat = (product.fatGrams * per100Multiplier).toCleanText()
        errorMessage = null
    }

    LaunchedEffect(repository) {
        repository.observeProducts().collect { products = it }
    }

    if (isViewingSavedProducts) {
        SavedProductsScreen(
            products = products,
            modifier = modifier,
            onBack = { isViewingSavedProducts = false },
            onEdit = { product -> startEditing(product) },
            onDelete = { product ->
                coroutineScope.launch {
                    repository.deleteProduct(product)
                    if (editingProduct?.id == product.id) {
                        clearForm()
                    }
                }
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
            ProductForm(
                editingProduct = editingProduct,
                name = name,
                brand = brand,
                category = category,
                portionAmount = portionAmount,
                portionUnit = portionUnit,
                calories = calories,
                protein = protein,
                carbohydrates = carbohydrates,
                fat = fat,
                errorMessage = errorMessage,
                onNameChange = { name = it },
                onBrandChange = { brand = it },
                onCategoryChange = { category = it },
                onPortionAmountChange = { portionAmount = it },
                onPortionUnitChange = { selectedUnit ->
                    portionUnit = selectedUnit
                    errorMessage = null
                },
                onCaloriesChange = { calories = it },
                onProteinChange = { protein = it },
                onCarbohydratesChange = { carbohydrates = it },
                onFatChange = { fat = it },
                onSave = {
                    val product = buildProductOrNull(
                        editingProduct = editingProduct,
                        name = name,
                        brand = brand,
                        category = category,
                        portionAmount = portionAmount,
                        portionUnit = portionUnit,
                        calories = calories,
                        protein = protein,
                        carbohydrates = carbohydrates,
                        fat = fat
                    )

                    if (product == null) {
                        errorMessage = "Enter a name, choose a category, and valid positive numbers."
                        return@ProductForm
                    }

                    coroutineScope.launch {
                        if (editingProduct == null) {
                            repository.addProduct(product)
                        } else {
                            repository.updateProduct(product)
                        }
                        clearForm()
                    }
                },
                onCancel = ::clearForm
            )
        }

        item {
            OutlinedButton(
                onClick = { isViewingSavedProducts = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View saved products (${products.size})")
            }
        }
    }
}

@Composable
private fun SavedProductsScreen(
    products: List<ProductEntity>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(CategoryFilterAll) }
    val categoryFilters = remember {
        listOf(CategoryFilterAll) + ProductCategory.entries.map { it.label }
    }
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == CategoryFilterAll) {
            products
        } else {
            products.filter { product ->
                product.category.orEmpty().ifBlank { ProductCategory.Other.label } == selectedCategory
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                text = "Saved products",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            CategoryFilterRow(
                categories = categoryFilters,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
        }
        if (products.isEmpty()) {
            item {
                Text(
                    text = "No products saved yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (filteredProducts.isEmpty()) {
            item {
                Text(
                    text = "No products in this category.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(filteredProducts, key = { it.id }) { product ->
                ProductListItem(
                    product = product,
                    onEdit = { onEdit(product) },
                    onDelete = { onDelete(product) }
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            if (category == selectedCategory) {
                Button(onClick = { onCategorySelected(category) }) {
                    Text(category)
                }
            } else {
                OutlinedButton(onClick = { onCategorySelected(category) }) {
                    Text(category)
                }
            }
        }
    }
}

@Composable
private fun ProductForm(
    editingProduct: ProductEntity?,
    name: String,
    brand: String,
    category: String,
    portionAmount: String,
    portionUnit: String,
    calories: String,
    protein: String,
    carbohydrates: String,
    fat: String,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPortionAmountChange: (String) -> Unit,
    onPortionUnitChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbohydratesChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (editingProduct == null) "Add product" else "Edit product",
            style = MaterialTheme.typography.headlineMedium
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Product name") },
            singleLine = true
        )
        OutlinedTextField(
            value = brand,
            onValueChange = onBrandChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Product brand") },
            singleLine = true
        )
        CategoryDropdown(
            selectedCategory = category,
            onCategorySelected = onCategoryChange,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = portionAmount,
                onValueChange = onPortionAmountChange,
                modifier = Modifier.weight(1f),
                label = { Text("Full portion size") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            UnitDropdown(
                selectedUnit = portionUnit,
                onUnitSelected = onPortionUnitChange,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = "Per 100 ml/g",
            style = MaterialTheme.typography.titleMedium
        )
        OutlinedTextField(
            value = calories,
            onValueChange = onCaloriesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroTextField(
                value = protein,
                onValueChange = onProteinChange,
                label = "Protein",
                modifier = Modifier.weight(1f)
            )
            MacroTextField(
                value = carbohydrates,
                onValueChange = onCarbohydratesChange,
                label = "Carbs",
                modifier = Modifier.weight(1f)
            )
            MacroTextField(
                value = fat,
                onValueChange = onFatChange,
                label = "Fat",
                modifier = Modifier.weight(1f)
            )
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSave) {
                Text(if (editingProduct == null) "Save" else "Update")
            }
            if (editingProduct != null) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedCategory)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ProductCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label) },
                    onClick = {
                        onCategorySelected(category.label)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UnitDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedUnit)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ProductUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.label) },
                    onClick = {
                        onUnitSelected(unit.label)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MacroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}

@Composable
private fun ProductListItem(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (product.brand.orEmpty().isNotBlank()) {
                Text(
                    text = product.brand.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = product.category.orEmpty().ifBlank { ProductCategory.Other.label },
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

private fun buildProductOrNull(
    editingProduct: ProductEntity?,
    name: String,
    brand: String,
    category: String,
    portionAmount: String,
    portionUnit: String,
    calories: String,
    protein: String,
    carbohydrates: String,
    fat: String
): ProductEntity? {
    val parsedPortionAmount = portionAmount.toDoubleOrNull()
    val parsedCalories = calories.toDoubleOrNull()
    val parsedProtein = protein.toDoubleOrNull()
    val parsedCarbohydrates = carbohydrates.toDoubleOrNull()
    val parsedFat = fat.toDoubleOrNull()
    val trimmedName = name.trim()
    val trimmedBrand = brand.trim()
    val selectedCategory = category.takeIf { candidate ->
        ProductCategory.entries.any { it.label == candidate }
    } ?: return null
    val trimmedUnit = portionUnit.trim()

    if (
        trimmedName.isEmpty() ||
        trimmedUnit.isEmpty() ||
        parsedPortionAmount == null ||
        parsedCalories == null ||
        parsedProtein == null ||
        parsedCarbohydrates == null ||
        parsedFat == null ||
        parsedPortionAmount <= 0 ||
        parsedCalories < 0 ||
        parsedProtein < 0 ||
        parsedCarbohydrates < 0 ||
        parsedFat < 0
    ) {
        return null
    }

    val nutritionMultiplier = parsedPortionAmount / 100.0

    return ProductEntity(
        editingProduct?.id ?: 0,
        trimmedName,
        trimmedBrand,
        selectedCategory,
        parsedPortionAmount,
        trimmedUnit,
        parsedCalories * nutritionMultiplier,
        parsedProtein * nutritionMultiplier,
        parsedCarbohydrates * nutritionMultiplier,
        parsedFat * nutritionMultiplier,
        editingProduct?.isFavorite ?: false,
        editingProduct?.createdAtMillis ?: System.currentTimeMillis()
    )
}

private enum class ProductCategory(val label: String) {
    Fruit("Fruit"),
    Meat("Meat"),
    Dairy("Dairy"),
    Drink("Drink"),
    Sweets("Sweets"),
    Grain("Grain"),
    Vegetable("Vegetable"),
    Other("Other")
}

private const val CategoryPlaceholder = "Choose category"
private const val CategoryFilterAll = "All"

private enum class ProductUnit(val label: String) {
    Ml("ml"),
    Grams("g")
}

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}
