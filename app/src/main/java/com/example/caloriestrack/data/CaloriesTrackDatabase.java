package com.example.caloriestrack.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                ProductEntity.class,
                FoodEntryEntity.class,
                GoalEntity.class,
                WeightLogEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class CaloriesTrackDatabase extends RoomDatabase {
    private static volatile CaloriesTrackDatabase instance;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE products ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0");
        }
    };
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE products ADD COLUMN brand TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE food_entries ADD COLUMN productBrand TEXT DEFAULT ''");
        }
    };

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
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
                }
            }
        }
        return instance;
    }
}
