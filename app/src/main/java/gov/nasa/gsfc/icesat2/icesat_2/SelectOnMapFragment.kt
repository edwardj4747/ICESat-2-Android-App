package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import kotlinx.android.synthetic.main.fragment_select_on_map.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "SelectOnMapFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [SelectOnMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectOnMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var chosenLocation: LatLng
    private lateinit var previousCircle: Circle
    private lateinit var listener: ISearchFragmentCallback
    private var seekBarValue = 12.5 //used to store the radius of the search

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d(TAG, "select on map fragment created")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_on_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapSelector) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //set up callbacks to go to MainActivity
        listener = requireActivity() as MainActivity

        //disable the seek bar until a marker is dropped
        seekBar.isEnabled = false
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarValue = (progress + 10.0) / 10 //seekbar ranges from 0 to 240
                //constraint required for searching
                if (seekBarValue == 1.0) {
                    seekBarValue = 1.1
                }
                Log.d(TAG, "progress changed. Progress is $seekBarValue")
                displayRadius()

                drawCircle(chosenLocation, seekBarValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        btnSearch.setOnClickListener {
            listener.searchButtonPressed(chosenLocation.latitude, chosenLocation.longitude, seekBarValue, true)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SelectOnMapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SelectOnMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "map ready")
        mMap = p0
        mMap.setOnMapLongClickListener(this)
    }

    override fun onMapLongClick(clickLocation: LatLng?) {
        Log.d(TAG, "Long clicked recorded @ $clickLocation")
        if (clickLocation != null) {
            chosenLocation = clickLocation
            val markerOptions = MarkerOptions()
            val stringLocation = Geocoding.getAddress(requireContext(), clickLocation.latitude, clickLocation.longitude)
            val truncatedLatLng = String.format("%.2f, %.2f", clickLocation.latitude, clickLocation.longitude)
            mMap.addMarker(markerOptions.position(clickLocation).title(stringLocation).snippet(truncatedLatLng)).showInfoWindow()
            seekBar.isEnabled = true
            //the default value of the seekbar
            drawCircle(chosenLocation, 12.5)
            //zoom in towards the pointer
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clickLocation, 9.25F))

            //set the text view
            displayRadius()
        }
    }

    private fun displayRadius() {
        val formattedString = String.format("%.1f miles\n%.1f kilometers", seekBarValue, seekBarValue * 1.60934)
        textViewDropPin.text = formattedString
    }

    private fun drawCircle(center: LatLng, radius: Double) {
        Log.d(TAG, "Drawing circle")
        val MILES_TO_METERS = 1609.34
        val circleOption = CircleOptions().center(center).radius(radius * MILES_TO_METERS)
        if (this::previousCircle.isInitialized) {
           previousCircle.remove()
        }
        previousCircle = mMap.addCircle(circleOption)
        Log.d(TAG, "drawing circle completes")
    }

}