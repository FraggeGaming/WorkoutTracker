package se.umu.cs.dv20fnh.gymlogger

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataBaseAdapter(application: Application) : AndroidViewModel(application){

    private val workoutDao: WorkoutDao

    init {
        val database = WorkoutDB.getDatabase(application)
        workoutDao = database.workoutDao()
    }

    suspend fun addExercise(exercise: String, fields: List<String>){

        if (fields.isEmpty() || exercise == "") return

        val ex = Exercise(name = exercise)
        val exerciseId = workoutDao.insertExercise(ex).toInt()

        //Insert trackers with the exercise ID for each field
        fields.forEach {value ->
            val tracker = Tracker(exerciseId = exerciseId, name = value)
            workoutDao.insertTracker(tracker)
        }


    }

    suspend fun addWorkout(exercise: String, date: String, fields: List<Pair<String, String>>){
        val hasEmptyOrNullValues = fields.any { (_, value) ->
            value.isEmpty()
        }

        if (hasEmptyOrNullValues || exercise.isEmpty()) return


        val exerciseId = workoutDao.getExerciseIdByName(exercise) ?: return

        //Auto-generate a group ID for these WorkoutData entries
        val generatedGroupId = workoutDao.getNextGroupId()

        val workout = Workout(
            exerciseId = exerciseId,
            date = date
        )
        val workoutId = workoutDao.insertWorkout(workout).toInt()

        //insert WorkoutData entries and associate them with the Workout
        fields.forEach { (tracker, value) ->
            val workoutData = WorkoutData(
                name = tracker,
                value = value.toIntOrNull() ?: 0,
                group = generatedGroupId,
                workoutId = workoutId
            )

            try {
                workoutDao.insertWorkoutData(workoutData)
            } catch (e: SQLiteConstraintException) {
                Log.e("Error", "Failed to insert WorkoutData: ${e.message}")
            }
        }


    }


    suspend fun getWorkoutData(exercise: String, startDate: String, endDate: String): Map<Workout, List<WorkoutData>> {

        val fetchedData = workoutDao.getSortedWorkouts(exercise, startDate, endDate)

        val workoutDataMap = mutableMapOf<Workout, MutableList<WorkoutData>>()

        fetchedData.forEach { data ->
            val workout = Workout(
                id = data.workoutId,
                exerciseId = data.workoutExerciseId,
                date = data.workoutDate
            )

            val workoutData = WorkoutData(
                id = data.workoutDataId,
                name = data.workoutDataName,
                value = data.workoutDataValue,
                group = data.workoutDataGroup,
                workoutId = data.workoutDataWorkoutId
            )

            workoutDataMap.computeIfAbsent(workout) { mutableListOf() }.add(workoutData)
        }

        return workoutDataMap
    }

    suspend fun getExercises(): List<String> {
        return workoutDao.getAllExerciseNames()

    }

    suspend fun getExerciseFields(exerciseName: String): List<String> {
        val exerciseId = workoutDao.getExerciseIdByName(exerciseName)

        val trackers = exerciseId?.let { workoutDao.getTrackersForExercise(it) }

        return trackers?.map { it.name } ?: emptyList()
    }

    suspend fun updateWorkoutData(workoutDataList: List<WorkoutData>) {
        workoutDataList.forEach { workoutData ->
            workoutDao.updateWorkoutData(workoutData.id, workoutData.value)
        }
    }

    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout.id)
    }

    suspend fun deleteExercise(exercise: String) {
        workoutDao.deleteExercise(exercise)
    }


}