package com.example.caloriestrack.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY createdAtMillis ASC")
    Flow<List<FoodEntryEntity>> observeEntriesForDate(String date);

    @Query("SELECT * FROM food_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAtMillis ASC")
    Flow<List<FoodEntryEntity>> observeEntriesBetweenDates(String startDate, String endDate);

    @Insert
    long insertEntry(FoodEntryEntity entry);

    @Update
    void updateEntry(FoodEntryEntity entry);

    @Delete
    void deleteEntry(FoodEntryEntity entry);

    @Query("DELETE FROM food_entries WHERE id = :id")
    void deleteEntryById(long id);
}
