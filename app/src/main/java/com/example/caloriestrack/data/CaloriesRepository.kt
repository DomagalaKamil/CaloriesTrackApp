package com.example.caloriestrack.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CaloriesRepository(
    private val productDao: ProductDao,
    private val foodEntryDao: FoodEntryDao,
    private val goalDao: GoalDao,
    private val weightLogDao: WeightLogDao
) {
    fun observeProducts() = productDao.observeAllProducts()

    suspend fun getProductById(id: Long) = withContext(Dispatchers.IO) {
        productDao.getProductById(id)
    }

    suspend fun addProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    suspend fun setProductFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        productDao.updateFavorite(id, isFavorite)
    }

    fun observeEntriesForDate(date: String) = foodEntryDao.observeEntriesForDate(date)

    fun observeEntriesBetweenDates(startDate: String, endDate: String) =
        foodEntryDao.observeEntriesBetweenDates(startDate, endDate)

    suspend fun addFoodEntry(entry: FoodEntryEntity) = withContext(Dispatchers.IO) {
        foodEntryDao.insertEntry(entry)
    }

    suspend fun updateFoodEntry(entry: FoodEntryEntity) = withContext(Dispatchers.IO) {
        foodEntryDao.updateEntry(entry)
    }

    suspend fun deleteFoodEntry(entry: FoodEntryEntity) = withContext(Dispatchers.IO) {
        foodEntryDao.deleteEntry(entry)
    }

    suspend fun deleteFoodEntryById(id: Long) = withContext(Dispatchers.IO) {
        foodEntryDao.deleteEntryById(id)
    }

    fun observeGoals() = goalDao.observeGoals(GoalEntity.DEFAULT_GOALS_ID)

    suspend fun getGoals() = withContext(Dispatchers.IO) {
        goalDao.getGoals(GoalEntity.DEFAULT_GOALS_ID)
    }

    suspend fun saveGoals(goals: GoalEntity) = withContext(Dispatchers.IO) {
        goalDao.upsertGoals(goals)
    }

    fun observeWeightLogs() = weightLogDao.observeAllWeightLogs()

    fun observeWeightLogsBetweenDates(startDate: String, endDate: String) =
        weightLogDao.observeWeightLogsBetweenDates(startDate, endDate)

    suspend fun saveWeightLog(weightLog: WeightLogEntity) = withContext(Dispatchers.IO) {
        weightLogDao.insertWeightLog(weightLog)
    }

    suspend fun deleteWeightLog(weightLog: WeightLogEntity) = withContext(Dispatchers.IO) {
        weightLogDao.deleteWeightLog(weightLog)
    }

    suspend fun deleteWeightLogById(id: Long) = withContext(Dispatchers.IO) {
        weightLogDao.deleteWeightLogById(id)
    }

    companion object {
        fun fromDatabase(database: CaloriesTrackDatabase): CaloriesRepository {
            return CaloriesRepository(
                productDao = database.productDao(),
                foodEntryDao = database.foodEntryDao(),
                goalDao = database.goalDao(),
                weightLogDao = database.weightLogDao()
            )
        }
    }
}
