package com.example.caloriestrack.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC, createdAtMillis DESC")
    Flow<List<WeightLogEntity>> observeAllWeightLogs();

    @Query("SELECT * FROM weight_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAtMillis ASC")
    Flow<List<WeightLogEntity>> observeWeightLogsBetweenDates(String startDate, String endDate);

    @Insert
    long insertWeightLog(WeightLogEntity weightLog);

    @Delete
    void deleteWeightLog(WeightLogEntity weightLog);

    @Query("DELETE FROM weight_logs WHERE id = :id")
    void deleteWeightLogById(long id);
}
