package gov.nasa.gsfc.icesat2.icesat_2.favoritesdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoritesDao {
    @Insert
    fun insert(favoritesEntry: FavoritesEntry)

    @Query("SELECT * FROM favorites_table WHERE dateObjectTime = :timeKey")
    fun contains(timeKey: Long): Array<FavoritesEntry>

    @Delete
    fun delete(favoritesEntry: FavoritesEntry)

    //creating custom behavior
    @Query("DELETE FROM favorites_table")
    fun deleteAllFavorites()

    @Query("SELECT * FROM favorites_table") //potentially order
    fun getAllFavorites(): LiveData<List<FavoritesEntry>>
}