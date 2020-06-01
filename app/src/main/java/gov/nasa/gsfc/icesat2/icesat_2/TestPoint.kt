package gov.nasa.gsfc.icesat2.icesat_2

import java.util.*

class TestPoint(val dateString: String, val dateObject: Date) {
    override fun toString(): String {
        return "dateString = $dateString, dateObject = ${dateObject.time}"
    }

    fun getDateObjectMethod(): Date {
        return dateObject
    }
}