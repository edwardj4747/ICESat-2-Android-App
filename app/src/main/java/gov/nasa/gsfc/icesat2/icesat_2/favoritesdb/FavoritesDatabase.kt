package gov.nasa.gsfc.icesat2.icesat_2.favoritesdb

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "FavoritesDatabase"

@Database(version = 1, entities = [FavoritesEntry::class], exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao //all necessary code for this method generated at runtime

    companion object {
        @Volatile
        private var instance: FavoritesDatabase? = null
        private val number_of_threads = 4
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(number_of_threads)

        fun getInstance(context: Context): FavoritesDatabase {
            if (instance == null) {
                Log.d(TAG, "getInstance: instance is null")
                synchronized(FavoritesDatabase::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            FavoritesDatabase::class.java, "favorites_table"
                        )
                            .fallbackToDestructiveMigration()
                            //.addCallback(roomCallback())
                            .build() //used builder because class is abstract
                    }
                }
            }
            return instance!!
        }
    }
}