package gov.nasa.gsfc.icesat2.icesat_2



import android.content.Context
import android.location.Geocoder

class Geocoding() {
    companion object {
        fun getAddress(context: Context, lat: Double, long: Double) : String {
            val geocoder = Geocoder(context)
            val address = geocoder.getFromLocation(lat, long, 1)
            return address[0].getAddressLine(0)
        }
    }

}