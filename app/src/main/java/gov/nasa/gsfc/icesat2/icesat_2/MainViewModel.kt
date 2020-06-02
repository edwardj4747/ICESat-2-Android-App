package gov.nasa.gsfc.icesat2.icesat_2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MainViewModel : ViewModel() {
    var allPointsList = MutableLiveData<ArrayList<Point>>()

    fun getAllPointsList(): LiveData<ArrayList<Point>> {
        return allPointsList
    }

    var allPointsChain = MutableLiveData<ArrayList<ArrayList<Point>>>()

    fun getAllPointsChain(): LiveData<ArrayList<ArrayList<Point>>> {
        return allPointsChain
    }

    var searchCenter = MutableLiveData<LatLng>()

    fun getSearchCenter(): LiveData<LatLng> {
        return searchCenter
    }

    val searchRadius = MutableLiveData<Double>()

    fun getSearchRadius(): LiveData<Double> {
        return searchRadius
    }
}