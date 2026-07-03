package com.example.caloriestrack.data;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    Flow<List<WeightLogEntity>> observeAllWeightLogs();

    @Query("SELECT * FROM weight_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    Flow<List<WeightLogEntity>> observeWeightLogsBetweenDates(String startDate, String endDate);

    @Upsert
    void upsertWeightLog(WeightLogEntity weightLog);

    @Query("DELETE FROM weight_logs WHERE date = :date")
    void deleteWeightLogByDate(String date);
}
