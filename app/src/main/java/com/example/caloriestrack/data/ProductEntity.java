package com.example.caloriestrack.data;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    @ColumnInfo(defaultValue = "''")
    public String brand;
    @ColumnInfo(defaultValue = "'Other'")
    public String category;
    public double basePortionAmount;
    public String basePortionUnit;
    public double calories;
    public double proteinGrams;
    public double carbohydrateGrams;
    public double fatGrams;
    @ColumnInfo(defaultValue = "0")
    public boolean isFavorite;
    public long createdAtMillis;

    public ProductEntity(
            long id,
            String name,
            String brand,
            String category,
            double basePortionAmount,
            String basePortionUnit,
            double calories,
            double proteinGrams,
            double carbohydrateGrams,
            double fatGrams,
            boolean isFavorite,
            long createdAtMillis
    ) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.basePortionAmount = basePortionAmount;
        this.basePortionUnit = basePortionUnit;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbohydrateGrams = carbohydrateGrams;
        this.fatGrams = fatGrams;
        this.isFavorite = isFavorite;
        this.createdAtMillis = createdAtMillis;
    }
}
