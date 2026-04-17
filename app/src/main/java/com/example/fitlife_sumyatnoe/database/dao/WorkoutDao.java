package com.example.fitlife_sumyatnoe.database.dao;

import androidx.room.*;
import com.example.fitlife_sumyatnoe.database.entities.WorkoutEntity;
import java.util.List;

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY name")
    List<WorkoutEntity> getAll();

    @Query("SELECT * FROM workouts WHERE isAdded = 1")
    List<WorkoutEntity> getAddedWorkouts();

    @Query("SELECT * FROM workouts WHERE id = :id")
    WorkoutEntity getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WorkoutEntity workout);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WorkoutEntity> workouts);

    @Update
    void update(WorkoutEntity workout);

    @Delete
    void delete(WorkoutEntity workout);

    @Query("DELETE FROM workouts")
    void deleteAll();
}