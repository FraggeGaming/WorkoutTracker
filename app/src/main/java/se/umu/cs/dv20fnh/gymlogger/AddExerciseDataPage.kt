package se.umu.cs.dv20fnh.gymlogger

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddExerciseDataPage(trackerModel: TrackerModel){

    private val model = trackerModel

    @Composable
    fun Main() {

        val exercises by model.exerciseList.collectAsState()
        val selectedExercise by model.selectedExercise.collectAsState()
        var trackerFields by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
        var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

        //Fetch exercises and update the dropdown options
        LaunchedEffect(selectedExercise, exercises) {
            model.fetchExercises()
            trackerFields =
                model.getTrackerList().map { name ->
                    name to ""
                }
        }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val toastMsg = stringResource(R.string.toast_save)

        fun handleSave() {

            val hasEmptyValues = trackerFields.any { (_, value) ->
                value.isEmpty()
            }

            if (!hasEmptyValues && selectedExercise.isNotEmpty()) {
                scope.launch(Dispatchers.IO) {
                    model.addWorkout(selectedDate, trackerFields)

                    trackerFields =
                        model.getTrackerList().map { name ->
                            name to ""
                        }
                }

            } else {
                //Show a Toast message if any field is empty
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            }
        }

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                // Dropdown menu for selecting an exercise

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    Components.DropdownMenu(
                        selected = selectedExercise,
                        onClick = {
                            model.setSelectedExercise(it)
                            scope.launch {
                            trackerFields = model.getTrackerList().map { name -> name to "" }
                            }
                        },
                        items = exercises
                    )
                }
            }

            items(trackerFields.size) { index ->
                val (trackerName, value) = trackerFields[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    TextField(
                        value = value,
                        onValueChange = { newValue ->
                            //Only numeric input is allowed
                            trackerFields = trackerFields.toMutableList().apply {
                                set(index, trackerName to newValue.filter { it.isDigit() })
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.LightGray,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTrailingIconColor = Color.Black,
                            unfocusedTrailingIconColor = Color.White,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.White,
                            unfocusedPlaceholderColor = Color.White,
                            focusedPlaceholderColor = Color.Black,
                        ),
                        modifier = Modifier.weight(1f),
                        label = { Text(trackerName) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            item {
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
                    },
                    LocalDate.now().year,
                    LocalDate.now().monthValue - 1,
                    LocalDate.now().dayOfMonth
                )

                //Display the selected date
                Text(
                    text = stringResource(R.string.selected_date) + " " + selectedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = {
                        datePickerDialog.show() //Show the DatePickerDialog
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.select_date),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.select_date))
                }

                Spacer(modifier = Modifier.height(16.dp))

                //Save Button
                Button(
                    onClick = { handleSave() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(stringResource(R.string.save))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    }
}

