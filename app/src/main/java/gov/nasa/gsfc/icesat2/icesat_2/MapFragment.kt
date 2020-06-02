package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


private const val ZOOM_LEVEL = 10f

class MapFragment : Fragment(), OnMapReadyCallback {

    private val TAG = "MapFragment"

    private lateinit var mMap: GoogleMap
    var markers = ArrayList<Marker>()
    private lateinit var pointChains: ArrayList<ArrayList<Point>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d(TAG, "onActivityCreated. Fragment being replaced")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        MainActivity.getMainViewModel()?.getAllPointsChain()?.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "=======Split into Chains Array===========")
            Log.d(TAG, "number of chains ${it.size}")
            for (i in 0 until it.size) {
                Log.d(TAG, "chain $i. size of chain ${it[i].size}: ${it[i]}")
            }
            pointChains = it
            if (this::mMap.isInitialized) {
                Log.d(TAG, "Add polylines from inside observer")
                for (i in 0 until it.size) {
                    addChainPolyline(it[i])
                }
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady starts")
        mMap = googleMap

        if (this::pointChains.isInitialized) {
            Log.d(TAG, "Adding polyline from inside onMapReady")
            for (i in 0 until pointChains.size) {
                addChainPolyline(pointChains[i])
            }
        }
    }

    private fun addChainPolyline(chain:ArrayList<Point>) {
        Log.d(TAG, "addChainPolyLine Starts")
        val polylineOptions = PolylineOptions()

        //test addign a marker at each point - maybe polygons are a better way to do this
        val myMarker = MarkerOptions()
        for (i in 0 until chain.size) {
            polylineOptions.add(LatLng(chain[i].latitude, chain[i].longitude))
            mMap.addMarker(myMarker.position(LatLng(chain[i].latitude, chain[i].longitude)).title(chain[i].dateString))
        }
        mMap.addPolyline(polylineOptions).apply {
            jointType = JointType.ROUND
            color = (0xff32CD32.toInt())
        }

        addCircleRadius(25.0)
    }

    private fun addCircleRadius(radius: Double) {
        val MILES_TO_METERS = 1609.34
        val circleOptions = CircleOptions().radius(radius * MILES_TO_METERS).center(LatLng(10.0, 10.0))
        mMap.addCircle(circleOptions)
        //mMap.addMarker(MarkerOptions().position(LatLng(10.0, 10.0)))

        val circle = mMap.addCircle(circleOptions)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(circleOptions.center, getZoomLevel(circle)))

        //moveCamera()
    }

    private fun moveCamera() {
        /*val radius = 25.0
        val MILES_PER_LATLNG = 69
        val offset = radius/MILES_PER_LATLNG
        val centerLat = 10.0
        val centerLong = 10.0
        val center = LatLng(centerLat, centerLong)
        val top = LatLng(centerLat + offset, centerLong)
        val bottom = LatLng(centerLat - offset, centerLong)
        val left = LatLng(centerLat, centerLong + offset)
        val right = LatLng(centerLat, centerLong - offset)
        //need to convert the 10 to have something to do with the radius

        val latLngBounds = LatLngBounds.builder().include(top).include(bottom).include(left).include(right).build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100)
        //val cameraUpdate = CameraUpdateFactory.newLatLng(LatLng(10.0, 10.0))
        mMap.animateCamera(cameraUpdate)*/


    }

    //https://stackoverflow.com/questions/11309632/how-to-find-zoom-level-based-on-circle-draw-on-map
    private fun getZoomLevel(circle: Circle?): Float {
        var zoomLevel = 11.0
        if (circle != null) {
            val radius = circle.radius + circle.radius / 2
            val scale = radius / 500
            zoomLevel = (16.25 - Math.log(scale) / Math.log(2.0))
        }
        return zoomLevel.toFloat()
    }

   /* private fun userLocation() {
        try {
            val locationResult = fusedLocationClient.lastLocation
            locationResult.addOnCompleteListener {
                Log.d(TAG, "location result complete")
                if (it.isSuccessful) {
                    Log.d(TAG, "was successful: $it")
                    val lastKnownLocation = it.result
                    val lat = lastKnownLocation?.latitude
                    val long = lastKnownLocation?.longitude
                    Log.d(TAG, "lat $lat and long $long")
                    if (lat != null && long != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, long), ZOOM_LEVEL))
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    Log.d(TAG, "location result listener failed ${it.exception}")
                }
            }
        } catch (e: SecurityException) {
            Log.d(TAG, "Security exception e: ${e.message}")
        }
    }*/

    private fun newPoint(lat: Double, long: Double) {
        Log.d(TAG, "newPoint method called")
        val newPoint = LatLng(lat, long);
        val newPointMarker = mMap.addMarker(MarkerOptions().position(newPoint).title("New Point"))
        markers.add(newPointMarker)
    }


    private fun draw(radius: Double) {
        val circleCenter = LatLng(-14.2, 144.5)
        mMap.addCircle(CircleOptions().radius(radius * 1609.34).center(circleCenter))

        val latLngBounds = LatLngBounds.builder().include(circleCenter).include(LatLng(-14.2 + 10, 144.5)).include(LatLng(-14.2 - 10, 144.5)).build()
        val padding = 100

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding))
    }

    private fun remove() {
        Log.d(TAG, "remove function called")
        Log.d(TAG, "markers.size = ${markers.size}")
        for (i in 0 until markers.size) {
            markers[i].remove()
        }
    }

}