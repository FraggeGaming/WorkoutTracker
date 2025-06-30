package se.umu.cs.dv20fnh.gymlogger

import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import java.util.Locale

@Parcelize
data class GraphTracker(
    val date: String = "",
    val selectedExercise: String = "",
    val selectedTracker: String = "",
    val workoutList: List<Workout> = emptyList(),
    val workoutDataList: List<List<WorkoutData>> = emptyList(),
    val exerciseList: List<String> = emptyList(),
    val trackerList: List<String> = emptyList(),
    val zoomScale: Float = 1.0f,
    val zoomCenterX: Float = 0.5f
) : Parcelable {
    //Method to reconstruct the map
    fun getWorkoutsWithData(): Map<Workout, List<WorkoutData>> {
        val map = mutableMapOf<Workout, List<WorkoutData>>()
        for (i in workoutList.indices) {
            map[workoutList[i]] = workoutDataList[i]
        }
        return map
    }

    //Method to create GraphTracker from a Map
    companion object {
        fun fromMap(
            date: String,
            selectedExercise: String,
            selectedTracker: String,
            map: Map<Workout, List<WorkoutData>>,
            exerciseList: List<String>,
            trackerList: List<String>,
            zoomScale: Float,
            zoomCenterX: Float
        ): GraphTracker {
            val workoutList = map.keys.toList()
            val workoutDataList = map.values.toList()
            return GraphTracker(
                date,
                selectedExercise,
                selectedTracker,
                workoutList,
                workoutDataList,
                exerciseList,
                trackerList,
                zoomScale,
                zoomCenterX
            )
        }
    }
}



/**
 * ViewModel to store the data in a parcel and stateflow
 * @param savedStateHandle: SavedStateHandle to store the parcel
 * */
class TrackerModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private lateinit var dbAdapter: DataBaseAdapter

    //Init the dataBase adapter
    fun initialize(dbAdapter: DataBaseAdapter) {
        this.dbAdapter = dbAdapter
    }


    //SavedStateHandle get and set
    private var graphTracker: GraphTracker
        get() {
            val tracker = savedStateHandle.get<GraphTracker>("graphTracker") ?: GraphTracker()
            Log.d("TrackerModel", "Retrieved GraphTracker: $tracker")
            return tracker
        }
        set(value) {
            savedStateHandle["graphTracker"] = value
            Log.d("TrackerModel", "Saved GraphTracker: $value")
        }




    //StateFlow variables
    private val _date = MutableStateFlow(graphTracker.date)
    val date: StateFlow<String> get() = _date

    private val _selectedExercise = MutableStateFlow(graphTracker.selectedExercise)
    val selectedExercise: StateFlow<String> get() = _selectedExercise

    private val _selectedTracker = MutableStateFlow(graphTracker.selectedTracker)
    val selectedTracker: StateFlow<String> get() = _selectedTracker

    private val _workoutsWithData = MutableStateFlow(graphTracker.getWorkoutsWithData())
    val workoutsWithData: StateFlow<Map<Workout, List<WorkoutData>>> get() = _workoutsWithData

    private val _exerciseList = MutableStateFlow(graphTracker.exerciseList)
    val exerciseList: StateFlow<List<String>> get() = _exerciseList

    private val _trackerList = MutableStateFlow(graphTracker.trackerList)
    val trackerList: StateFlow<List<String>> get() = _trackerList

    private val _zoomScale = MutableStateFlow(graphTracker.zoomScale)
    val zoomScale: StateFlow<Float> get() = _zoomScale

    private val _zoomCenterX = MutableStateFlow(graphTracker.zoomCenterX)
    val zoomCenterX: StateFlow<Float> get() = _zoomCenterX



    init {
        //Ensure that the date is set to the first day of the month
        if (_date.value.isEmpty()) {
            val initialDate = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            setDate(initialDate)
        }
    }

    //Update methods
    fun setDate(newDate: String) {
        _date.value = newDate
        graphTracker = graphTracker.copy(date = newDate)
    }

    fun setPrevMonth(){
        setDate(addMonthToDate(_date.value, -1))
    }

    fun setNextMonth(){
        setDate(addMonthToDate(_date.value, 1))
    }

    fun setSelectedExercise(newExercise: String) {
        _selectedExercise.value = newExercise
        graphTracker = graphTracker.copy(selectedExercise = newExercise)
    }

    fun setSelectedTracker(newTracker: String) {
        _selectedTracker.value = newTracker
        graphTracker = graphTracker.copy(selectedTracker = newTracker)
    }

    private fun setWorkoutsWithData(map: Map<Workout, List<WorkoutData>>) {
        _workoutsWithData.value = map
        graphTracker = GraphTracker.fromMap(
            _date.value, _selectedExercise.value, _selectedTracker.value,
            _workoutsWithData.value, _exerciseList.value, _trackerList.value, _zoomScale.value, _zoomCenterX.value
        )
    }

    fun setZoomScale(newScale: Float) {
        _zoomScale.value = newScale
        graphTracker = graphTracker.copy(zoomScale = newScale)
    }

    fun setZoomCenterX(newCenterX: Float) {
        _zoomCenterX.value = newCenterX
        graphTracker = graphTracker.copy(zoomCenterX = newCenterX)
    }

    //increase and cap the zoom level
    fun multiplyZoom(factor: Float) {
        _zoomScale.value = (zoomScale.value * factor)
        _zoomScale.value = _zoomScale.value.coerceIn(0.7f, 15f)

        graphTracker = graphTracker.copy(zoomScale = _zoomScale.value)
    }

    //Pan for the X-axis
    fun addZoomCenterOffset(offset: Float) {
        val newZoomCenterX = zoomCenterX.value + offset
        _zoomCenterX.value = newZoomCenterX
        graphTracker = graphTracker.copy(zoomCenterX = newZoomCenterX)
    }

    private fun setTrackerList(newTrackerList: List<String>) {
        _trackerList.value = newTrackerList
        graphTracker = graphTracker.copy(trackerList = newTrackerList)
    }

    suspend fun getTrackerList(): List<String> {
        return dbAdapter.getExerciseFields(_selectedExercise.value)
    }

    suspend fun fetchWorkoutsNoLimit() {
        if (_date.value.isEmpty()) {
            setDate(LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        }
        val currentDate = LocalDate.parse(_date.value, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val nextMonth = currentDate.plusMonths(1).withDayOfMonth(1)
        val nextMonthDateString = nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        setWorkoutsWithData(dbAdapter.getWorkoutData(_selectedExercise.value, _date.value, nextMonthDateString))

        fetchTrackers()
    }

    suspend fun fetchExercises() {
        val exercises = dbAdapter.getExercises()
        setExercises(exercises)
        if (_selectedExercise.value.isEmpty() && exercises.isNotEmpty()) {
            setSelectedExercise(exercises[0]) //TODO try and then fix
        }
    }

    private suspend fun fetchTrackers() {
        setTrackerList(dbAdapter.getExerciseFields(_selectedExercise.value))
        if (_selectedTracker.value.isEmpty() && !getTrackerList().contains(_selectedTracker.value))
            setSelectedTracker(getTrackerList()[0])
    }

    private fun setExercises(exercises: List<String>) {
        _exerciseList.value = exercises
        graphTracker = graphTracker.copy(exerciseList = exercises)
    }

    suspend fun deleteWorkout(workout: Workout) {
        dbAdapter.deleteWorkout(workout)
        fetchWorkoutsNoLimit()
    }

    suspend fun addWorkout(date: String, trackerFields : List<Pair<String, String>> ){
        dbAdapter.addWorkout(_selectedExercise.value, date ,trackerFields)
    }

    suspend fun updateWorkoutData(data: List<WorkoutData>){
        dbAdapter.updateWorkoutData(data)
    }

    suspend fun createExercise(title: String, trackerValues: List<String>) {
        dbAdapter.addExercise(title, trackerValues)
    }

    suspend fun deleteExercise(exercise: String) {
        dbAdapter.deleteExercise(exercise)
    }


    fun createWorkoutDataMap(specifiedData: String, defaultTrackers: List<String>): MutableMap<String, MutableList<Pair<String, Float>>> {
        val map: MutableMap<String, MutableList<Pair<String, Float>>> = mutableMapOf()

        val workoutsWithData = workoutsWithData.value

        workoutsWithData.forEach { (workout, workoutDataList) ->

            var add = 0
            var multiplier = 1

            if (specifiedData == defaultTrackers[0]) {
                workoutDataList.forEach { workoutData ->
                    map.getOrPut(workoutData.name) { mutableListOf() }.add(workout.date to workoutData.value.toFloat())
                }
            }

            else if (specifiedData == defaultTrackers[1]){
                workoutDataList.forEach { workoutData ->
                    add += workoutData.value
                }

                map.getOrPut("Additive") { mutableListOf() }.add(workout.date to add.toFloat())
            }

            else if (specifiedData == defaultTrackers[2]) {
                workoutDataList.forEach { workoutData ->
                    multiplier *= workoutData.value
                }

                map.getOrPut("Multiplied") { mutableListOf() }.add(workout.date to multiplier.toFloat())
            }

            else {
                workoutDataList.forEach { workoutData ->
                    if (workoutData.name == specifiedData) {
                        map.getOrPut(workoutData.name) { mutableListOf() }.add(workout.date to workoutData.value.toFloat())
                    }
                }
            }
        }

        return map
    }


    private fun addMonthToDate(date: String, monthsToAdd: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localDate = LocalDate.parse(date, formatter)
        val newDate = localDate.plusMonths(monthsToAdd)
        return newDate.format(formatter)
    }

}