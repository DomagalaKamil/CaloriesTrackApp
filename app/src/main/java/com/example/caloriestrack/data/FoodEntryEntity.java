package com.example.caloriestrack.data;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "food_entries",
        indices = {
                @Index("date"),
                @Index("productId")
        }
)
public class FoodEntryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String date;
    public Long productId;
    public String productName;
    @ColumnInfo(defaultValue = "''")
    public String productBrand;
    public double amount;
    public String unit;
    public double calories;
    public double proteinGrams;
    public double carbohydrateGrams;
    public double fatGrams;
    public long createdAtMillis;

    public FoodEntryEntity(
            long id,
            String date,
            Long productId,
            String productName,
            String productBrand,
            double amount,
            String unit,
            double calories,
            double proteinGrams,
            double carbohydrateGrams,
            double fatGrams,
            long createdAtMillis
    ) {
        this.id = id;
        this.date = date;
        this.productId = productId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.amount = amount;
        this.unit = unit;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbohydrateGrams = carbohydrateGrams;
        this.fatGrams = fatGrams;
        this.createdAtMillis = createdAtMillis;
    }
}
