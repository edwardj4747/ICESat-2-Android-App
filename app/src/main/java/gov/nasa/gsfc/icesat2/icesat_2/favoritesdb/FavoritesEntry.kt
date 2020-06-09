package gov.nasa.gsfc.icesat2.icesat_2.favoritesdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
class FavoritesEntry(val dateObjectTime: Long, val dateString: String, val lat: Double, val lng: Double) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}