package com.example.caloriestrack.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class GoalEntity {
    public static final int DEFAULT_GOALS_ID = 1;

    @PrimaryKey
    public int id;
    public double dailyCalorieGoal;
    public double weeklyCalorieGoal;
    public long updatedAtMillis;

    public GoalEntity(
            int id,
            double dailyCalorieGoal,
            double weeklyCalorieGoal,
            long updatedAtMillis
    ) {
        this.id = id;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.weeklyCalorieGoal = weeklyCalorieGoal;
        this.updatedAtMillis = updatedAtMillis;
    }
}
