package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.location.Geocoder

private const val TAG = "Geocoding"

interface IGeocoding {

    fun getAddress(context: Context, lat: Double, long: Double) : String {
        val geocoder = Geocoder(context)
        val address = geocoder.getFromLocation(lat, long, 1)
        return try {
            address[0].getAddressLine(0)
        } catch (e: Exception) {
            context.getString(R.string.unknownLocation)
        }
    }

    fun getGeographicInfo(geocoder: Geocoder, lat: Double, long: Double): String {
        val address = geocoder.getFromLocation(lat, long, 1)

        if (address.size == 0) {
            return "Unknown Location"
        }

        //returns {locality, 'state', country}
        var locationString = ""


        if (!address[0].locality.isNullOrEmpty()) {
            locationString += "${address[0].locality}, "
        }
        if (!address[0].adminArea.isNullOrEmpty()) {
            locationString += "${address[0].adminArea}, "
        }
        if (!address[0].countryName.isNullOrEmpty()) {
            locationString += "${address[0].countryName}; "
        }

        if (locationString == "") {
            locationString = "Unknown Location, "
        }

        return locationString
    }
}

