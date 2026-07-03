package com.example.caloriestrack.data;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface GoalDao {
    @Query("SELECT * FROM goals WHERE id = :id")
    Flow<GoalEntity> observeGoals(int id);

    @Query("SELECT * FROM goals WHERE id = :id")
    GoalEntity getGoals(int id);

    @Upsert
    void upsertGoals(GoalEntity goals);
}
