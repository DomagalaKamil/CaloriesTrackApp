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
        version = 5,
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
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE products ADD COLUMN category TEXT DEFAULT 'Other'");
        }
    };
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS weight_logs_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "weightKg REAL NOT NULL, " +
                    "createdAtMillis INTEGER NOT NULL)");
            database.execSQL("INSERT INTO weight_logs_new (date, weightKg, createdAtMillis) " +
                    "SELECT date, weightKg, createdAtMillis FROM weight_logs");
            database.execSQL("DROP TABLE weight_logs");
            database.execSQL("ALTER TABLE weight_logs_new RENAME TO weight_logs");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_weight_logs_date ON weight_logs(date)");
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
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build();
                }
            }
        }
        return instance;
    }
}
