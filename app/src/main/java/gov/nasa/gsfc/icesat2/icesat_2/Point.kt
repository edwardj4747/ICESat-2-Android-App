package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
/*class Point (val date: String, val time: String, val longitude: Double, val latitude: Double, val dateString: String, val dateObject: Date) :
    Parcelable {

    override fun toString(): String {
        //return "date = $date time = $time lon = $longitude lat = $latitude"
        //return "convertedDate = $convertedDateTime lon = $longitude lat = $latitude"
        //return "convertedDate = $convertedDateTime date = ${dateObject.time}"
        return "$dateString, "
    }

}*/

class Point(val dateString: String, val dayOfWeek: String, val date: String, val year: String,
            val time: String, val ampm: String, val timezone: String, val longitude: Double, val latitude: Double, val dateObject: Date) :
    Parcelable {

    override fun toString(): String {
        return """
            $dayOfWeek $date $year $time $ampm $timezone 
        """.trimIndent()
    }
}