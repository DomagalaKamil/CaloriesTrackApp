package com.example.caloriestrack.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    Flow<List<ProductEntity>> observeAllProducts();

    @Query("SELECT * FROM products WHERE id = :id")
    ProductEntity getProductById(long id);

    @Insert
    long insertProduct(ProductEntity product);

    @Update
    void updateProduct(ProductEntity product);

    @Delete
    void deleteProduct(ProductEntity product);

    @Query("DELETE FROM products WHERE id = :id")
    void deleteProductById(long id);

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavorite(long id, boolean isFavorite);
}
