package com.example.caloriestrack.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "weight_logs")
public class WeightLogEntity {
    @PrimaryKey
    @NonNull
    public String date;
    public double weightKg;
    public long createdAtMillis;

    public WeightLogEntity(
            @NonNull String date,
            double weightKg,
            long createdAtMillis
    ) {
        this.date = date;
        this.weightKg = weightKg;
        this.createdAtMillis = createdAtMillis;
    }
}
