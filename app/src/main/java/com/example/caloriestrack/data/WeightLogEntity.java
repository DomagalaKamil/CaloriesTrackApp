package com.example.caloriestrack.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(
        tableName = "weight_logs",
        indices = {
                @Index("date")
        }
)
public class WeightLogEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String date;
    public double weightKg;
    public long createdAtMillis;

    public WeightLogEntity(
            long id,
            @NonNull String date,
            double weightKg,
            long createdAtMillis
    ) {
        this.id = id;
        this.date = date;
        this.weightKg = weightKg;
        this.createdAtMillis = createdAtMillis;
    }
}
