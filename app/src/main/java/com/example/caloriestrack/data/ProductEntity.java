package com.example.caloriestrack.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public double basePortionAmount;
    public String basePortionUnit;
    public double calories;
    public double proteinGrams;
    public double carbohydrateGrams;
    public double fatGrams;
    public long createdAtMillis;

    public ProductEntity(
            long id,
            String name,
            double basePortionAmount,
            String basePortionUnit,
            double calories,
            double proteinGrams,
            double carbohydrateGrams,
            double fatGrams,
            long createdAtMillis
    ) {
        this.id = id;
        this.name = name;
        this.basePortionAmount = basePortionAmount;
        this.basePortionUnit = basePortionUnit;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbohydrateGrams = carbohydrateGrams;
        this.fatGrams = fatGrams;
        this.createdAtMillis = createdAtMillis;
    }
}
