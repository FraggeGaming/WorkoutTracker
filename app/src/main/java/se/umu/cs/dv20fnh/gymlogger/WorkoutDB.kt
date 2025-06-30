package se.umu.cs.dv20fnh.gymlogger

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/**
 * Room database for the tracker
 * */
@Database(entities = [Exercise::class, Tracker::class, Workout::class, WorkoutData::class], version = 1)
abstract class WorkoutDB : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDB? = null

        fun getDatabase(context: Context): WorkoutDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDB::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

