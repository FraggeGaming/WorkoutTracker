package se.umu.cs.dv20fnh.gymlogger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface WorkoutDao {

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Int)

    @Query("DELETE FROM exercises")
    suspend fun clearExercises()

    @Query("DELETE FROM trackers")
    suspend fun clearTrackers()

    @Query("DELETE FROM workouts")
    suspend fun clearWorkouts()

    @Query("DELETE FROM workout_data")
    suspend fun clearWorkoutData()

    @Transaction
    suspend fun clearAllData() {
        clearWorkoutData()
        clearWorkouts()
        clearTrackers()
        clearExercises()
    }

    data class WorkoutAndData(
        val workoutId: Int,
        val workoutExerciseId: Int,
        val workoutDate: String,
        val workoutDataId: Int,
        val workoutDataName: String,
        val workoutDataValue: Int,
        val workoutDataGroup: Int,
        val workoutDataWorkoutId: Int
    )

    @Query("""
    SELECT 
        w.id AS workoutId,
        w.exerciseId AS workoutExerciseId,
        w.date AS workoutDate,
        wd.id AS workoutDataId,
        wd.name AS workoutDataName,
        wd.value AS workoutDataValue,
        wd.`group` AS workoutDataGroup,
        wd.workoutId AS workoutDataWorkoutId
    FROM 
        workouts AS w
    JOIN 
        workout_data AS wd ON w.id = wd.workoutId
    WHERE 
        w.exerciseId = (
            SELECT id FROM exercises WHERE name = :exerciseName LIMIT 1
        ) AND date >= :startDate AND w.date <= :endDate
    ORDER BY 
        w.date ASC, w.id DESC, wd.id ASC
""")
    suspend fun getSortedWorkouts(exerciseName: String, startDate: String, endDate: String): List<WorkoutAndData>


    @Query("""
    UPDATE workout_data
    SET 
        value = :newValue
    WHERE 
        id = :id
""")
    suspend fun updateWorkoutData(id: Int, newValue: Int)

    @Query("SELECT * FROM workout_data WHERE id = :id")
    suspend fun getWorkoutDataById(id: Int): WorkoutData

    @Query("SELECT * FROM workout_data WHERE name = :name AND `group` = :group")
    suspend fun getWorkoutDataByNameAndGroup(name: String, group: Int): WorkoutData


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: Tracker): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutData(workoutData: WorkoutData): Long

    @Query("SELECT IFNULL(MAX(`group`), 0) + 1 FROM workout_data")
    suspend fun getNextGroupId(): Int

    @Query("SELECT id FROM exercises WHERE name = :exerciseName LIMIT 1")
    suspend fun getExerciseIdByName(exerciseName: String): Int?

    @Query("SELECT * FROM trackers WHERE exerciseId = :exerciseId")
    suspend fun getTrackersForExercise(exerciseId: Int): List<Tracker>

    @Query("SELECT DISTINCT name FROM exercises")
    suspend fun getAllExerciseNames(): List<String>

    @Query("DELETE FROM exercises WHERE name = :exerciseName")
    suspend fun deleteExercise(exerciseName: String)

}


