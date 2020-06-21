package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private const val TAG = "SingleMarkerMap"

class SingleMarkerMap : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val args by navArgs<SingleMarkerMapArgs>()
    private lateinit var title: String
    private var lat = 0.0
    private var long = 0.0
    private var markerDisplayed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_marker_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lat = args.lat.toDouble()
        long = args.long.toDouble()
        title = args.title

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        val markerPosition = LatLng(lat, long)
        val marker = mMap.addMarker(MarkerOptions().position(markerPosition).title(title))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15F))
        mMap.setOnCameraIdleListener {
            Log.d(TAG, "camera is idle")
            if (!markerDisplayed) {
                markerDisplayed = true
                marker.showInfoWindow()
            }
        }
    }

}