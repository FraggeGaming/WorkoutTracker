package se.umu.cs.dv20fnh.gymlogger

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource

class ManageTrackersPage(trackerModel: TrackerModel) {

    private val model = trackerModel

    @Composable
    fun Main() {
        val selectedExercise by model.selectedExercise.collectAsState()
        val workoutsWithData by model.workoutsWithData.collectAsState()
        val dateRange by model.date.collectAsState()
        val exercises by model.exerciseList.collectAsState()

        LaunchedEffect(Unit, selectedExercise, dateRange) {

            model.fetchExercises()
            model.fetchWorkoutsNoLimit()
        }


        val configuration = LocalConfiguration.current

        //If landscape mode or portrait
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                TrackerDataCards(
                    workoutsWithData,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                )


                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Components.DropdownMenu(
                        selected = selectedExercise,
                        onClick = {
                            model.setSelectedExercise(it)
                        },
                        items = exercises
                    )

                    //change date
                    Components.GraphControls(
                        onPreviousDateRange = { model.setPrevMonth() },
                        onNextDateRange = { model.setNextMonth() },
                        theme = isSystemInDarkTheme(),
                        dateRange = dateRange
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {

                        Components.DropdownMenu(
                            selected = selectedExercise,
                            onClick = {
                                model.setSelectedExercise(it)
                            },
                            items = exercises
                        )
                    }
                }

                //change date
                Components.GraphControls(
                    onPreviousDateRange = { model.setPrevMonth() },
                    onNextDateRange = { model.setNextMonth() },
                    theme = isSystemInDarkTheme(),
                    dateRange = dateRange
                )

                TrackerDataCards(
                    workoutsWithData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }


    /**
     * Cards for showing the workouts with data.
     * @param workoutsWithData A map of workouts to their associated workout data.
     * @param modifier
     * */
    @Composable
    private fun TrackerDataCards(workoutsWithData: Map<Workout, List<WorkoutData>>, modifier: Modifier = Modifier){

        var showDialog by remember { mutableStateOf(false) }
        var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
        var editedWorkoutData by remember { mutableStateOf<List<TextFieldValue>>(emptyList()) }

        val scope = rememberCoroutineScope()
        LazyColumn(
            modifier = modifier
        ) {
            items(workoutsWithData.entries.toList()) { (workout, data) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)

                            .background(
                                Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = workout.date,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        data.forEach { workoutData ->
                            Text(
                                text = "${workoutData.name}: ${workoutData.value}",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    showDialog = true
                                    selectedWorkout = workout
                                    editedWorkoutData = data.map { TextFieldValue(it.value.toString()) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White,
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = stringResource(R.string.edit_f)
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        model.deleteWorkout(workout)
                                        model.fetchWorkoutsNoLimit()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White,
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_f),
                                )
                            }
                        }
                    }
                }
            }
        }

        //Show dialog for the edit workout section
        if (showDialog && selectedWorkout != null) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.edit_tracker),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        selectedWorkout?.let { workout ->
                            workoutsWithData[workout]?.forEachIndexed { index, workoutData ->
                                TextField(
                                    value = editedWorkoutData[index],
                                    onValueChange = { newValue ->
                                        editedWorkoutData = editedWorkoutData.toMutableList().apply {
                                            this[index] = newValue
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text(workoutData.value.toString()) },
                                    label = { Text(workoutData.name) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showDialog = false }) {
                                Text(text = stringResource(R.string.cancel))
                            }
                            TextButton(onClick = {
                                //Update the workout data
                                selectedWorkout?.let { workout ->
                                    val updatedData = workoutsWithData[workout]?.mapIndexed { index, workoutData ->
                                        workoutData.copy(value = editedWorkoutData[index].text.toIntOrNull() ?: workoutData.value)
                                    } ?: emptyList()


                                    scope.launch {
                                        model.updateWorkoutData(updatedData)
                                    }
                                }
                                showDialog = false
                            }) {
                                Text(text = stringResource(R.string.save))
                            }
                        }
                    }
                }
            }

        }
    }
}