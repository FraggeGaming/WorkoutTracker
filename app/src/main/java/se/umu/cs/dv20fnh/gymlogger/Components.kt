package se.umu.cs.dv20fnh.gymlogger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


/**
 * Class to store components for the app
 *
 * so far only stores the graph controls
 * */
class Components {


    companion object {


        /**
         * Dropdown menu component
         *
         * @param selected current selected item
         * @param onClick what to happen to the selected item onclick
         * @param items items of the dropdown
         * */
        @Composable
        @OptIn(ExperimentalMaterial3Api::class)
        fun DropdownMenu(selected: String, onClick: (it: String) -> Unit, items: List<String>){
            var expanded by remember { mutableStateOf(false) }

            //Select tracker dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {expanded = !expanded}
            ) {
                TextField(
                    value = if (selected == "") stringResource(R.string.trackers) else selected,
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.LightGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTrailingIconColor = Color.Black,
                        unfocusedTrailingIconColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { exerciseName ->
                        DropdownMenuItem(
                            text = { Text(exerciseName) },
                            onClick = {
                                onClick(exerciseName)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        /**
         * Change the date of the viewmodel
         *
         * @param onPreviousDateRange callback to previous date range
         * @param onNextDateRange callback to next date range
         * @param theme theme of the app
         * @param dateRange date graph
         * */
        @Composable
        fun GraphControls(
            onPreviousDateRange: () -> Unit,
            onNextDateRange: () -> Unit,
            theme: Boolean,
            dateRange: String
        ) {

            fun changeDateFormat(date: String): String {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
                val d = LocalDate.parse(date, formatter)

                val displayFormatter = DateTimeFormatter.ofPattern("yyyy MMM", Locale.getDefault())
                val da = d.format(displayFormatter)

                return da
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = onPreviousDateRange) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_prev_dr),
                            tint = if (theme) Color.White else Color.Black
                        )
                    }


                    Text(
                        text = changeDateFormat(dateRange),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (theme) Color.White else Color.Black
                    )

                    IconButton(onClick = onNextDateRange) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.cd_next_dr),
                            tint = if (theme) Color.White else Color.Black
                        )
                    }
                }

            }
        }
    }
}