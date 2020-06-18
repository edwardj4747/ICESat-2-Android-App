package gov.nasa.gsfc.icesat2.icesat_2.favoritesdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
class FavoritesEntry(@ColumnInfo(name = "dateObjectTime") val dateObjectTime: Long, val dateString: String, val lat: Double,
                     val lng: Double, val geocodedLocation: String) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}