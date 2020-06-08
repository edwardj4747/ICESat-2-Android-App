package gov.nasa.gsfc.icesat2.icesat_2.ui.favorites

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import gov.nasa.gsfc.icesat2.icesat_2.FavoritesRepository
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry

class FavoritesViewModel(@NonNull application: Application) : AndroidViewModel(application) {

    private var repository: FavoritesRepository = FavoritesRepository(application)
    private var allFavoritesData: LiveData<List<FavoritesEntry>>

    init {
        allFavoritesData = repository.getAllFavorites()
    }

    fun insert(favoritesEntry: FavoritesEntry) {
        repository.insert(favoritesEntry)
    }

    fun delete(favoritesEntry: FavoritesEntry) {
        repository.delete(favoritesEntry)
    }

    fun deleteAll() {
        repository.deleteAllFavorites()
    }

    fun getAllFavorites(): LiveData<List<FavoritesEntry>> {
        return allFavoritesData
    }
}