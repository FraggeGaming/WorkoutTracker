package se.umu.cs.dv20fnh.gymlogger

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.res.Configuration
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

class GraphPage(trackerModel: TrackerModel){

    private val model = trackerModel

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Main() {
        val exercises by model.exerciseList.collectAsState()
        val selectedExercise by model.selectedExercise.collectAsState()
        val workoutsWithData by model.workoutsWithData.collectAsState()

        val selectedTracker by model.selectedTracker.collectAsState()
        val trackerFields by model.trackerList.collectAsState()

        val dateRange by model.date.collectAsState()

        val defaultTrackers = LocalContext.current.resources.getStringArray(R.array.default_trackers).toList()


        LaunchedEffect(Unit, selectedExercise, selectedTracker, dateRange) {

            //Set the default tracker if it's not selected
            if (selectedTracker.isEmpty()) {
                model.setSelectedTracker(defaultTrackers[0])
            }

            //Fetch exercises and workoutdata
            model.fetchExercises()
            model.fetchWorkoutsNoLimit()
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        //Landscape layout
        if (isLandscape) {

            Row(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .weight(0.7f)

                ) {
                    Graph(
                        workoutsWithData = workoutsWithData,
                        specifiedData = selectedTracker,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(0.3f)) {

                    //Select tracker dropdown
                    Components.DropdownMenu(
                        selected = selectedExercise,
                        onClick = { model.setSelectedExercise(it) },
                        items = exercises
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    //Select subtracker dropdown
                    Components.DropdownMenu(
                        selected = selectedTracker,
                        onClick = { model.setSelectedTracker(it) },
                        items = defaultTrackers + trackerFields
                    )


                    //Change the date
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
            //Layout for portrait mode
            LazyColumn(modifier = Modifier.padding(16.dp)) {

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(end = 8.dp)
                        ) {
                            //Select tracker dropdown

                            Components.DropdownMenu(
                                selected = selectedExercise,
                                onClick = { model.setSelectedExercise(it) },
                                items = exercises
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.5f)

                        ) {

                            Components.DropdownMenu(
                                selected = selectedTracker,
                                onClick = { model.setSelectedTracker(it) },
                                items = defaultTrackers + trackerFields
                            )
                        }
                    }
                }

                //Change the date
                item {
                    Components.GraphControls(
                        onPreviousDateRange = { model.setPrevMonth() },
                        onNextDateRange = { model.setNextMonth() },
                        theme = isSystemInDarkTheme(),
                        dateRange = dateRange
                    )
                }

                item {
                    Graph(
                        workoutsWithData = workoutsWithData,
                        specifiedData = selectedTracker,
                    )
                }
            }
        }
    }


    /**
     *
     * Shows graph of the workout data
     * @param workoutsWithData - Map of workouts to their data
     * @param specifiedData - what subtracker to look for in the data
     * */
    @Composable
    fun Graph(
        workoutsWithData: Map<Workout, List<WorkoutData>>,
        specifiedData: String,

    ) {
        val defaultTrackers = LocalContext.current.resources.getStringArray(R.array.default_trackers).toList()

        //Used to remember the zoom and pan of the graph
        val zoomScale by model.zoomScale.collectAsState()
        val zoomCenterX by model.zoomCenterX.collectAsState()


        Column(
            modifier = Modifier
                .padding(bottom = 64.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {


            if (workoutsWithData.isEmpty()) {
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                    Text(stringResource(R.string.no_workouts), color = Color.Gray)
                }

            } else {

                //Dependning on the specified tracker, create the workout-data/value map
                val map = model.createWorkoutDataMap(specifiedData, defaultTrackers)

                //Map the subtrackers to different colors for the UI
                val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan)
                val colorMappedData = map.entries.zip(colors).map { (entry, color) ->
                    ColoredTrackerData(
                        color = color,
                        name = entry.key,
                        dataPoints = entry.value,
                    )
                }

                DisplayColorAndName(colorMappedData)

                //Shows the graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .height(300.dp)
                ) {
                    CustomGraph(colorMappedData, model, zoomScale, zoomCenterX)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    // Add a reset zoom button below the graph
                    Button(
                        onClick = {
                            model.setZoomScale(1f)
                            model.setZoomCenterX(0f)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor =  MaterialTheme.colorScheme.onBackground,

                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .width(200.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.reset_graph))
                    }
                }
            }
        }

    }
}

/**
 * Holds the workoutdata combined with a color and name
 *
 * */
data class ColoredTrackerData(
    val color: Color,
    val name: String,
    val dataPoints: List<Pair<String, Float>>
)

/**
 * Displays the subtrackers with the color
 * @param colorMappedData - list of the colormapped data
 * */
@Composable
fun DisplayColorAndName(colorMappedData: List<ColoredTrackerData>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)

    ){
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            colorMappedData.forEach { data ->

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(50))
                        .background(data.color)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = data.name, style = MaterialTheme.typography.titleMedium, color = Color.Black)

                Spacer(modifier = Modifier.width(16.dp))

            }
        }
    }

}

/**
 * helper function to draw the left border of the graph
 * @param width - width of the canvas
 * @param height - height of the canvas
 * @param max - max value of the y-axis
 * @param min - min value of the y-axis
 * @param darkMode - if the theme is dark, change the line colors
 * */
fun DrawScope.DrawGraphLeftBorders(
    width: Float,
    height: Float,
    max: Float,
    min: Float,
    darkMode: Boolean
){

    var lineColor: Color = Color.Black
    var secondLineColor: Color = Color.Gray

    if (darkMode){
        lineColor = Color.White
        secondLineColor = Color.LightGray
    }

    //Get the min and max values for the y-axis
    val maxValue = max
    val minValue = min

    //Horizontal grid lines
    val numberOfGridLines = 5
    val gridSpacing = height / numberOfGridLines


    //Draw the axis lines
    drawLine(
        color = lineColor,
        start = Offset(0f, height),
        end = Offset(width, height),
        strokeWidth = 4f
    )
    drawLine(
        color = lineColor,
        start = Offset(0f, 0f),
        end = Offset(0f, height),
        strokeWidth = 4f
    )

    //Draw the horizontal grid lines and y-axis labels
    for (i in 0..numberOfGridLines) {
        val y = i * gridSpacing
        drawLine(
            color = secondLineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )

        val value = maxValue - (i * (maxValue - minValue) / numberOfGridLines)
        drawContext.canvas.nativeCanvas.drawText(
            value.toInt().toString(),
            -60f,
            y + 5f,
            Paint().apply {
                color = if (!darkMode) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                textSize = 32f
                textAlign = android.graphics.Paint.Align.LEFT
            }
        )
    }
}

/**
 * Graph to draw the data points
 *
 * @param parsedData - list of the colormapped data
 * @param model - tracker viewmodel
 * @param scale - zoom scale
 * @param offsetX - zoom offset (x axis pan)
 * */
@Composable
fun CustomGraph(
    parsedData: List<ColoredTrackerData>,
    model: TrackerModel,
    scale: Float,
    offsetX: Float
) {

    val theme = isSystemInDarkTheme()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()

                //Look for pan and zoom gestures on the canvas and update the viewmodel accordingly
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        model.multiplyZoom(zoom)
                        model.addZoomCenterOffset(pan.x)
                    }
                }
        ) {
            if (parsedData.isEmpty()) {
                return@Canvas
            }

            val allValues = parsedData.flatMap { it.dataPoints }
            val maxValue = allValues.maxOf { it.second }
            val minValue = allValues.minOf { it.second }.coerceAtMost(maxValue - 1f)


            //Draw the Y-axis with labels
            DrawGraphLeftBorders(size.width, size.height, maxValue, minValue, theme)


            val leftPadding = 20.dp.toPx()

            //Adjust spacing in between points based on the zoom scale
            val baseXSpacing =
                (size.width - leftPadding) / (parsedData[0].dataPoints.size - 1).coerceAtLeast(1)
            val adjustedXSpacing = baseXSpacing * scale

            val paint = Paint().apply {
                color = if (theme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }

            //Measure the width of the text in the dataset
            val textWidth = parsedData.firstOrNull()?.dataPoints?.map { (date, _) ->
                paint.measureText(
                    LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM d"))
                ) + 20f
            }?.maxOrNull() ?: 100f

            //Determine if the individual dates should be drawn or a range based on the number of points on the graph
            val shouldDrawIndividualDates =
                parsedData[0].dataPoints.size < (scale * size.width) / textWidth

            //Draw the data points
            parsedData.forEach { coloredData ->
                val path = Path()


                val points = coloredData.dataPoints.mapIndexed { index, (date, value) ->
                    //Calculate the position for each point when zoomed and panned
                    val xPosition = leftPadding + index * adjustedXSpacing + offsetX
                    val yPosition =
                        size.height - (value - minValue) / (maxValue - minValue) * size.height

                    Triple(xPosition, Offset(xPosition, yPosition), date)
                }
                //Filter out points outside the canvas
                .filter { it.second.x in 0f..size.width }

                if (points.isNotEmpty()) {

                    //Use cubic interpolation for the lines to get somewhat rounded corners
                    path.moveTo(points.first().second.x, points.first().second.y)


                    for (i in 0 until points.size - 1) {
                        val p0 = if (i > 0) points[i - 1].second else points[i].second
                        val p1 = points[i].second
                        val p2 = points[i + 1].second
                        val p3 = if (i < points.size - 2) points[i + 2].second else points[i + 1].second

                        val cp1 = Offset(
                            p1.x + (p2.x - p0.x) / 20,
                            p1.y + (p2.y - p0.y) / 20
                        )
                        val cp2 = Offset(
                            p2.x - (p3.x - p1.x) / 20,
                            p2.y - (p3.y - p1.y) / 20
                        )

                        path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
                    }

                    //Draw the line connecting the points
                    drawPath(path, color = coloredData.color, style = Stroke(width = 4f))
                }

                //Draw the rounded points on the canvas
                points.forEach {(xPosition, point, date) ->
                    drawCircle(
                        color = coloredData.color,
                        radius = 16f,
                        center = point
                    )

                    //If all dates of the points can be shown, draw them
                    if (shouldDrawIndividualDates) {
                        val formattedDate =
                            LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM d"))

                        drawContext.canvas.nativeCanvas.drawText(
                            formattedDate,
                            xPosition,
                            size.height + 64f,
                            paint
                        )
                    }
                }
            }

            //if not, format and draw the date range
            if (!shouldDrawIndividualDates) {
                val firstDate = LocalDate.parse(parsedData.first().dataPoints.first().first)
                val lastDate = LocalDate.parse(parsedData.last().dataPoints.last().first)

                val formattedLabel = "${firstDate.format(DateTimeFormatter.ofPattern("MMM d"))} - ${
                    lastDate.format(
                        DateTimeFormatter.ofPattern("MMM d")
                    )
                }"

                drawContext.canvas.nativeCanvas.drawText(
                    formattedLabel,
                    size.width / 2,
                    size.height + 64f,
                    Paint().apply {
                        color = if (theme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                        textSize = 40f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}



