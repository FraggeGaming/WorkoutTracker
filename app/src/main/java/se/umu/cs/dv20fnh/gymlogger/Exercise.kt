package se.umu.cs.dv20fnh.gymlogger

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["name"], unique = true)]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String //referenced in Workout
)


@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exerciseId"])]
)

@Parcelize
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,  //References the `id` from Exercise
    val date: String
) : Parcelable

@Entity(
    tableName = "trackers",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exerciseId"])]
)
data class Tracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,  //References the `id` from Exercise
    val name: String
)


@Entity(
    tableName = "workout_data",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workoutId"])]
)

@Parcelize
data class WorkoutData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val value: Int,
    val group: Int,  //Grouping attribute to categorize workout data
    val workoutId: Int  //References the Workout table
) : Parcelable






