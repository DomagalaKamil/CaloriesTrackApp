package com.example.caloriestrack.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                ProductEntity.class,
                FoodEntryEntity.class,
                GoalEntity.class,
                WeightLogEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class CaloriesTrackDatabase extends RoomDatabase {
    private static volatile CaloriesTrackDatabase instance;

    public abstract ProductDao productDao();

    public abstract FoodEntryDao foodEntryDao();

    public abstract GoalDao goalDao();

    public abstract WeightLogDao weightLogDao();

    public static CaloriesTrackDatabase getDatabase(Context context) {
        if (instance == null) {
            synchronized (CaloriesTrackDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CaloriesTrackDatabase.class,
                            "calories_track.db"
                    ).build();
                }
            }
        }
        return instance;
    }
}
