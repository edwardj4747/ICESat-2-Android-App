package gov.nasa.gsfc.icesat2.icesat_2

import android.app.Application
import androidx.lifecycle.LiveData
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesDao
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesDatabase
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "FavoritesRepository"

class FavoritesRepository(application: Application) {
    private var favoritesDao: FavoritesDao
    private var allFavoritesData: LiveData<List<FavoritesEntry>>

    private var results = false

    init {
        val database: FavoritesDatabase = FavoritesDatabase.getInstance(application)
        favoritesDao = database.favoritesDao()
        allFavoritesData = favoritesDao.getAllFavorites()
    }

    fun insert(favoritesEntry: FavoritesEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            favoritesDao.insert(favoritesEntry)
        }
    }

    fun contains(timeKey: Long): Boolean = runBlocking(Dispatchers.IO) {
        favoritesDao.contains(timeKey).isNotEmpty()
    }

    fun delete(favoritesEntry: FavoritesEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            favoritesDao.delete(favoritesEntry)
        }
    }


    fun deleteAllFavorites() {
        CoroutineScope(Dispatchers.IO).launch {
            favoritesDao.deleteAllFavorites()
        }
    }

    fun getAllFavorites(): LiveData<List<FavoritesEntry>> {
        return allFavoritesData
    }
}