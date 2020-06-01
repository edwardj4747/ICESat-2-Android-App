package gov.nasa.gsfc.icesat2.icesat_2

import androidx.lifecycle.MutableLiveData
import java.util.*

class Point (val date: String, val time: String, val longitude: Double, val latitude: Double, val convertedDateTime: String, val dateObject: Date) {

    override fun toString(): String {
        //return "date = $date time = $time lon = $longitude lat = $latitude"
        //return "convertedDate = $convertedDateTime lon = $longitude lat = $latitude"
        //return "convertedDate = $convertedDateTime date = ${dateObject.time}"
        return "$convertedDateTime, "
    }

}