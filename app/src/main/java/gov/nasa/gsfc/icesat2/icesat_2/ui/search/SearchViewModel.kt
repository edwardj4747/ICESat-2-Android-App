package gov.nasa.gsfc.icesat2.icesat_2.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gov.nasa.gsfc.icesat2.icesat_2.Point

class SearchViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    var allPointsList = MutableLiveData<ArrayList<Point>>()

    fun getAllPointsList(): LiveData<ArrayList<Point>> {
        return allPointsList
    }
}