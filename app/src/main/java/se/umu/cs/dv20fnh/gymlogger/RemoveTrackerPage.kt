package se.umu.cs.dv20fnh.gymlogger


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class RemoveTrackerPage(trackerModel: TrackerModel){

    private val model = trackerModel

    @Composable
    fun Main() {


        val exercises by model.exerciseList.collectAsState()
        val selectedExercise by model.selectedExercise.collectAsState()
        val scope = rememberCoroutineScope()

        //Fetch trackers
        LaunchedEffect(Unit) {
            model.fetchExercises()
        }

        Column(modifier = Modifier.padding(16.dp)) {
            //Dropdown menu for selecting an tracker

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {

                //Select subtracker dropdown
                Components.DropdownMenu(
                    selected = selectedExercise,
                    onClick = {
                        model.setSelectedExercise(it)
                    },
                    items = exercises
                )
            }

            //Delete Tracker button
            Button(
                onClick = {
                    if (selectedExercise.isNotEmpty()) {
                        scope.launch {
                            model.deleteExercise(selectedExercise)
                            model.fetchExercises()
                            model.setSelectedExercise(model.exerciseList.value.first())
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = selectedExercise.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_tracker)
                )
            }
        }
    }
}