package se.umu.cs.dv20fnh.gymlogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {


    private lateinit var trackerModel: TrackerModel
    private lateinit var dbAdapter: DataBaseAdapter
    private lateinit var graphPage: GraphPage
    private lateinit var addExerciseDataPage: AddExerciseDataPage
    private lateinit var manageTrackersPage: ManageTrackersPage

    private lateinit var createTrackerPage: CreateTrackerPage
    private lateinit var removeTrackerPage: RemoveTrackerPage

    private lateinit var tutorialManager: TutorialManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tutorialManager = TutorialManager(this)


        //Init database and viewmodel
        dbAdapter = ViewModelProvider(this)[DataBaseAdapter::class.java]


        trackerModel = ViewModelProvider(this)[TrackerModel::class.java]
        trackerModel.initialize(dbAdapter)


        graphPage = GraphPage(trackerModel)
        addExerciseDataPage = AddExerciseDataPage(trackerModel)
        manageTrackersPage = ManageTrackersPage(trackerModel)

        createTrackerPage = CreateTrackerPage(trackerModel)
        removeTrackerPage = RemoveTrackerPage(trackerModel)


        enableEdgeToEdge()
        setContent {

            val lightColors = lightColorScheme(
                primary = Color(ContextCompat.getColor(this, R.color.nav_color)),
                onPrimary = Color(ContextCompat.getColor(this, R.color.on_primary)),
                secondary = Color(ContextCompat.getColor(this, R.color.button_color)),
                background = Color(ContextCompat.getColor(this, R.color.back_color)),
                surface = Color(ContextCompat.getColor(this, R.color.box_color)),
                onBackground = Color(ContextCompat.getColor(this, R.color.text_color)),
                onSurface = Color(ContextCompat.getColor(this, R.color.on_surface)),
            )

            val darkColors = darkColorScheme(
                primary = Color(ContextCompat.getColor(this, R.color.d_nav_color)),
                onPrimary = Color(ContextCompat.getColor(this, R.color.d_on_primary)),
                secondary = Color(ContextCompat.getColor(this, R.color.d_button_color)),
                background = Color(ContextCompat.getColor(this, R.color.d_back_color)),
                surface = Color(ContextCompat.getColor(this, R.color.d_box_color)),
                onBackground = Color(ContextCompat.getColor(this, R.color.d_text_color)),
                onSurface = Color(ContextCompat.getColor(this, R.color.d_on_surface)),
            )


            val colorMap = if (isSystemInDarkTheme()) darkColors else lightColors

            MaterialTheme(
                colorScheme = colorMap
            ) {
                val navColor = MaterialTheme.colorScheme.primary

                val systemUiController = rememberSystemUiController()

                // Set the status bar as the same color as navigation bar
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = navColor
                    )
                    systemUiController.setNavigationBarColor(
                        color = navColor
                    )
                }

                //Jetpack navigate for navigation between screens
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = ThirdScreen)
                {

                    composable<SecondScreen> {
                        ScreenLayout(navController, {AddRemovePage(navController)}, 1)

                    }

                    composable<ThirdScreen> {

                        ScreenLayout(navController, {graphPage.Main()}, 0)

                    }

                    composable<ManageTrackerPage> {

                        ScreenLayout(navController, {manageTrackersPage.Main()}, 2)
                    }

                    composable<CreateExerciseScreen> {

                        ScreenLayout(navController, {createTrackerPage.Main()}, -1)
                    }

                    composable<AddExerciseScreen> {

                        ScreenLayout(navController, {addExerciseDataPage.Main()}, -1)

                    }

                    composable<RemoveExerciseScreen> {

                        ScreenLayout(navController, {removeTrackerPage.Main()}, -1)
                    }
                }
            }

        }
    }

    /**
     * Displays a tutorial card over the screen.
     *
     * @param onDismiss tutorial finish button onClick
     * @param modifier
     */
    @Composable
    fun TutorialCard(
        onDismiss: () -> Unit,
        modifier: Modifier
    ){

        val buttonColor = MaterialTheme.colorScheme.secondary
        val buttonTextColor = MaterialTheme.colorScheme.onBackground

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.t_welcome), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.t_text))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onDismiss() }
                    ,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = buttonTextColor,

                        ),) {
                        Text(stringResource(R.string.t_button_text))
                    }
                }
            }
        }
    }


    /**
     * Card Button component.
     *
     * @param title button title
     * @param onClick onclick function
     * @param icon button icon
     * @param buttonColor button color
     * @param buttonTextColor text color
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CardButton(
        title: String,
        onClick: () -> Unit,
        icon: ImageVector,
        buttonColor: Color,
        buttonTextColor: Color
    ){
        Card(
            onClick = onClick,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(150.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = buttonColor,
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = buttonTextColor
                )
                Spacer(modifier = Modifier.height(40.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = buttonTextColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }


    /**
    * Page to add/remove/create trackers.
    *
    * @param navController jetpack compose navigation controller
    */
    @Composable
    fun AddRemovePage(navController: NavHostController){

        val buttonColor = MaterialTheme.colorScheme.secondary
        val buttonTextColor = MaterialTheme.colorScheme.onBackground

        LazyColumn(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize()
                ,
            verticalArrangement = Arrangement.Center
        ) {

            item {
                CardButton(
                    title = stringResource(R.string.create_tracker),
                    buttonColor = buttonColor,
                    buttonTextColor = buttonTextColor,
                    onClick = {navController.navigate(CreateExerciseScreen)},
                    icon = Icons.Default.Create
                )
            }

            item {
                CardButton(
                    title = stringResource(R.string.add_tracker),
                    onClick = {navController.navigate(AddExerciseScreen)},
                    icon = Icons.Default.Add,
                    buttonColor = buttonColor,
                    buttonTextColor = buttonTextColor
                )
            }

            item {
                CardButton(
                    title = stringResource(R.string.remove_tracker),
                    onClick = {navController.navigate(RemoveExerciseScreen)},
                    icon = Icons.Default.Delete,
                    buttonColor = buttonColor,
                    buttonTextColor = buttonTextColor
                )
            }
        }
    }


    /**
     * Standard layout for all screens
     *
     * @param navController jetpack compose navigation controller
     * @param content the main content of the screen
     * @param selectIndex the index of the selected menu, used for status indication
     */
    @Composable
    fun ScreenLayout(
        navController: NavHostController,
        content: @Composable () -> Unit,
        selectIndex: Int,
    ){

        val isTutorialCompleted by tutorialManager.isTutorialCompleted.collectAsState(initial = false)

        val backColor =  MaterialTheme.colorScheme.background
        val navColor = MaterialTheme.colorScheme.primary
        val buttonTextColor = MaterialTheme.colorScheme.onBackground

        Box(modifier = Modifier
            .fillMaxSize()
            .background(backColor)
            .systemBarsPadding()) {

            //Back button and title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(navColor)
            ) {

                Button(
                    onClick = {
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = navColor,
                        contentColor = buttonTextColor
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = buttonTextColor // Or any other color
                    )
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = buttonTextColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }


            //Main content
            Column(
                modifier = Modifier
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top // Start content from the top but with padding
            ) {
                content()

            }

            //Tutorial section if first time use
            if (!isTutorialCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .zIndex(1f)
                ) {
                    TutorialCard(
                        onDismiss = {

                            lifecycleScope.launch {
                                tutorialManager.setTutorialCompleted(true)
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Navigation Bar at the bottom
            NavBar(
                navController = navController,
                i = selectIndex,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    /**
     * Navigation bar at the bottom.
     *
     * @param navController jetpack compose navigation controller
     * @param i the selected button index
     * @param modifier
     */
    @Composable
    fun NavBar(
        navController: NavHostController,
        i: Int,
        modifier: Modifier
    ){

        val navColor = MaterialTheme.colorScheme.primary
        val buttonTextColor = MaterialTheme.colorScheme.onBackground

        Row(
            modifier = modifier
                .fillMaxWidth()
            ,
            horizontalArrangement = Arrangement.Start
        ) {
           //navigation button modifiers
            val buttonModifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(navColor)

            Button(
                onClick = {
                    navController.navigate(ThirdScreen)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = navColor,
                    contentColor = buttonTextColor,
                ),
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.cd_graph),
                    tint = if (i == 0) Color.Gray else buttonTextColor
                )
            }

            CustomDivider()

            Button(
                onClick = {
                    navController.navigate(SecondScreen)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = navColor,
                    contentColor = buttonTextColor,
                ),
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add),
                    tint = if (i == 1) Color.Gray else buttonTextColor
                )
            }

            CustomDivider()

            Button(
                onClick = {
                    navController.navigate(ManageTrackerPage)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = navColor,
                    contentColor = buttonTextColor,

                ),
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.cd_manage),
                    tint = if (i == 2) Color.Gray else buttonTextColor
                )
            }
        }
    }

    /**
     * Divider for the navigation bar between the buttons.
     */
    @Composable
    fun CustomDivider() {
        val backColor =  MaterialTheme.colorScheme.background
        val navColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .height(48.dp)
                .width(1.dp)
        ) {
            //Top part
            Spacer(
                modifier = Modifier
                    .background(navColor)
                    .fillMaxWidth()
                    .height(4.8.dp)
                    .align(Alignment.TopCenter)
            )

            //Center part
            Spacer(
                modifier = Modifier
                    .background(backColor)
                    .fillMaxWidth()
                    .height(38.4.dp)
                    .align(Alignment.Center)
            )

            //Bottom part
            Spacer(
                modifier = Modifier
                    .background(navColor)
                    .fillMaxWidth()
                    .height(4.8.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }


    //Screen navigation objects
    @Serializable
    object AddExerciseScreen

    @Serializable
    object RemoveExerciseScreen

    @Serializable
    object CreateExerciseScreen

    @Serializable
    object SecondScreen

    @Serializable
    object ThirdScreen

    @Serializable
    object ManageTrackerPage

}