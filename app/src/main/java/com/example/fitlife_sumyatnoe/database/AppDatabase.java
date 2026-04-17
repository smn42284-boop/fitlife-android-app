package com.example.fitlife_sumyatnoe.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.fitlife_sumyatnoe.database.dao.ExerciseDao;
import com.example.fitlife_sumyatnoe.database.dao.WorkoutDao;
import com.example.fitlife_sumyatnoe.database.entities.ExerciseEntity;
import com.example.fitlife_sumyatnoe.database.entities.WorkoutEntity;

@Database(entities = {WorkoutEntity.class, ExerciseEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseDao exerciseDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fitlife_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}