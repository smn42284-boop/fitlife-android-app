package com.example.fitlife_sumyatnoe.database.dao;

import androidx.room.*;
import com.example.fitlife_sumyatnoe.database.entities.ExerciseEntity;
import java.util.List;

@Dao
public interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY position")
    List<ExerciseEntity> getByWorkoutId(String workoutId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExerciseEntity exercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExerciseEntity> exercises);

    @Update
    void update(ExerciseEntity exercise);

    @Delete
    void delete(ExerciseEntity exercise);

    @Query("DELETE FROM exercises WHERE workoutId = :workoutId")
    void deleteByWorkoutId(String workoutId);

    @Query("DELETE FROM exercises")
    void deleteAll();
}