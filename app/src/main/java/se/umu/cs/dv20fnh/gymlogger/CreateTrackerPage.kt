package se.umu.cs.dv20fnh.gymlogger

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class CreateTrackerPage(trackerModel: TrackerModel){

    private val model = trackerModel

    @Composable
    fun Main() {

        // State for managing the dynamic form fields
        var title by remember { mutableStateOf(TextFieldValue("")) }

        // State for the list of text field values
        var fields by remember { mutableStateOf(listOf<TextFieldValue>()) }

        val scope = rememberCoroutineScope()

        fun handleSave() {
            // Access the DAO from the database
            scope.launch {
                // Convert the list of TextFieldValues to a list of Strings before passing to the DAO
                val fieldValues = fields.map { it.text }
                model.createExercise(title.text, fieldValues)

                model.setSelectedExercise(title.text)

                // Clear the form fields after saving
                title = TextFieldValue("")
                fields = listOf()
            }
        }

        LazyColumn(modifier = Modifier.padding(16.dp)) {

            item {
                //Title Input Field
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.LightGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedTextColor = Color.Black,  // Text color when focused
                        unfocusedTextColor = Color.White,  // Text color when not focused
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,  // Underline color when focused
                        unfocusedIndicatorColor = Color.Transparent,  // Underline color when not focused
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.White,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.White,
                        unfocusedPlaceholderColor = Color.White,
                        focusedPlaceholderColor = Color.Black,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.title)) }
                )

                Spacer(modifier = Modifier.height(16.dp))


            }
            //Loop through the list of fields and create input rows
            items(fields.size) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = fields[index],
                        onValueChange = { newValue ->
                            fields = fields.mapIndexed { i, field ->
                                if (i == index) newValue else field
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.LightGray,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedTextColor = Color.Black,  // Text color when focused
                            unfocusedTextColor = Color.White,  // Text color when not focused
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,  // Underline color when focused
                            unfocusedIndicatorColor = Color.Transparent,  // Underline color when not focused
                            focusedTrailingIconColor = Color.Black,
                            unfocusedTrailingIconColor = Color.White,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.White,
                            unfocusedPlaceholderColor = Color.White,
                            focusedPlaceholderColor = Color.Black,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text(stringResource(R.string.field)) },
                    )

                    // Remove Button
                    IconButton(
                        onClick = {
                            fields = fields.filterIndexed { i, _ -> i != index }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.remove),
                            tint = Color.Red
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart // Aligns the button to the right
                ) {
                    Button(
                        onClick = {
                            fields = fields + TextFieldValue("")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary, // Background color
                            contentColor = MaterialTheme.colorScheme.onBackground // Text color
                        ),
                        shape = CircleShape, // Make the button round
                        enabled = fields.size < 6
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Save Button
                Button(
                    onClick = { handleSave() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    enabled = fields.all { it.text.isNotEmpty() } && title.text.isNotEmpty()
                ) {
                    Text(stringResource(R.string.save))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }


        }
    }

}