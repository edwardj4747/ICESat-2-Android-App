package gov.nasa.gsfc.icesat2.icesat_2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import kotlinx.android.synthetic.main.fragment_select_on_map.*

private const val TAG = "SelectOnMapFragment"

class SelectOnMapFragment : Fragment(), IGeocoding, OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var chosenLocation: LatLng
    private lateinit var previousCircle: Circle
    private lateinit var marker: Marker
    private lateinit var listener: ISearchFragmentCallback
    private var seekBarValue = 12.5 //used to store the radius of the search
    private var stringLocation = ""


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

        //if there is a marker, set the text to display radius and modify radius of circle
        if (this::marker.isInitialized) {
            displayRadius()
            drawCircle(chosenLocation, seekBarValue)
        }

        //if there is no marker disable the seekbar, but if there is a marker then enable it
        seekBar.isEnabled = this::marker.isInitialized

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
            if (seekBar.isEnabled) {
                listener.searchButtonPressed(chosenLocation.latitude, chosenLocation.longitude, seekBarValue, true)
                MainActivity.getMainViewModel()?.searchString?.value = stringLocation
            }
        }
    }


    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "map ready")
        mMap = p0
        mMap.setOnMapLongClickListener(this)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null && !this::marker.isInitialized) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 5F))
                }
            }

        }
    }

    override fun onMapLongClick(clickLocation: LatLng?) {
        Log.d(TAG, "Long clicked recorded @ $clickLocation")
        if (clickLocation != null) {
            if (this::marker.isInitialized) {
                marker.remove()
            }
            chosenLocation = clickLocation
            val markerOptions = MarkerOptions()
            //stringLocation = Geocoding.getAddress(requireContext(), clickLocation.latitude, clickLocation.longitude)
            //method in IGeocoding interface
            stringLocation = getAddress(requireContext(), clickLocation.latitude, clickLocation.longitude)
            val truncatedLatLng = String.format("%.2f, %.2f", clickLocation.latitude, clickLocation.longitude)
            marker = mMap.addMarker(markerOptions.position(clickLocation).title(stringLocation).snippet(truncatedLatLng))
            marker.showInfoWindow()
            seekBar.isEnabled = true
            //set the radius of the circle to be the radius of the seekbar
            drawCircle(chosenLocation, seekBarValue)
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