package gov.nasa.gsfc.icesat2.icesat_2

import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*
import java.util.*

private const val TAG = "MapFragment"

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, IMarkerSelectedCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var pointChains: ArrayList<ArrayList<Point>>
    private lateinit var searchCenter: LatLng
    private var searchRadius: Double = -1.0
    private lateinit var fm: FragmentManager
    private lateinit var markerSelectedFragment: MarkerSelectedFragment
    private var marker: Marker? = null //used to keep track of the selected marker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)
        fm = childFragmentManager

        Log.d(TAG, "onActivityCreated. Fragment being replaced")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mainActivityViewModel = MainActivity.getMainViewModel()

        mainActivityViewModel?.getAllPointsChain()?.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "=======Split into Chains Array===========")
            Log.d(TAG, "number of chains ${it.size}")
            for (i in 0 until it.size) {
                Log.d(TAG, "chain $i. size of chain ${it[i].size}: ${it[i]}")
            }

            //if no results show a dialog explaining that there are no results
            if (it.size == 0) {
               val alertBuilder = AlertDialog.Builder(requireContext())
                alertBuilder.setMessage(R.string.noResultsDetails)
                    ?.setTitle(R.string.noResults)
                    ?.setPositiveButton(R.string.backToSearch) { dialog, which -> Log.d(TAG, "Dialog positive button clicked") }
                alertBuilder.show()
            }


            pointChains = it
            if (this::mMap.isInitialized) {
                Log.d(TAG, "Add polylines from inside observer")
                for (i in 0 until it.size) {
                    addChainPolyline(it[i])
                }
            }
        })

        mainActivityViewModel?.getSearchCenter()?.observe(viewLifecycleOwner, Observer{
            Log.d(TAG, "MapFragment searchCenterObserved to be ${it.latitude}, ${it.longitude}")
            searchCenter = it
        })

        mainActivityViewModel?.getSearchRadius()?.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "MapFrag searchRadius observed to be $it")
            searchRadius = it
        })

        btnAddEvent.setOnClickListener {
            addToCalendar(getString(R.string.icesatFlyover), pointChains[0][0].dateObject, pointChains[0][0].latitude, pointChains[0][0].longitude)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady starts")
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

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


        //test adding a marker at each point - maybe polygons are a better way to do this
        val myMarker = MarkerOptions()
        for (i in 0 until chain.size) {
            polylineOptions.add(LatLng(chain[i].latitude, chain[i].longitude))
            mMap.addMarker(myMarker.position(LatLng(chain[i].latitude, chain[i].longitude)).title(chain[i].dateString))
        }

        mMap.addPolyline(polylineOptions).apply {
            jointType = JointType.ROUND
            color = (0xff32CD32.toInt())
        }

        addCircleRadius(searchRadius)
    }

    private fun addCircleRadius(radius: Double) {
        val MILES_TO_METERS = 1609.34
        val circleOptions = CircleOptions().radius(radius * MILES_TO_METERS).center(searchCenter)
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

    //TODO: Make sure the size of DummyFragment is always the same as MarkerSelectionFragment
    override fun onMarkerClick(p0: Marker?): Boolean {
        marker = p0
        val fragmentTransaction = fm.beginTransaction()
        markerSelectedFragment = MarkerSelectedFragment.newInstance(p0!!.title, p0.title)
        fragmentTransaction.apply {
            setCustomAnimations(R.anim.slide_in_down, R.anim.blank_animation)
            replace(R.id.mapFragmentContainer, markerSelectedFragment)
            commit()
        }
        return false
    }

    override fun onMapClick(p0: LatLng?) {
        marker = null
        if (this::markerSelectedFragment.isInitialized) {
            fm.beginTransaction().apply {
                setCustomAnimations(R.anim.blank_animation, R.anim.slide_out_down)
                replace(R.id.mapFragmentContainer, DummyFragment())
                commit()
            }
        }
    }

    override fun closeButtonPressed() {
        onMapClick(null)
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


    private fun addToCalendar(title: String, startTime: Date, lat: Double, long: Double) {
        val cityLocation = geocodeLocation(lat, long)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.time)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime.time + 60 * 1000) //end time is one minute later
            putExtra(CalendarContract.Events.EVENT_LOCATION, cityLocation)
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun geocodeLocation(lat: Double, long: Double) : String {
        Log.d(TAG, "geocode location starts")
        val geocoder = Geocoder(requireContext())
        val address = geocoder.getFromLocation(lat, long, 1)
        //Log.d(TAG, "adress is $address")
        Log.d(TAG, "address line ${address[0].getAddressLine(0)}")
       /* Log.d(TAG, "address line ${address[0].getAddressLine(1)}")
        Log.d(TAG, "address line ${address[0].getAddressLine(2)}")
        Log.d(TAG, "address line ${address[0].getAddressLine(3)}")*/
        Log.d(TAG, "admin area is ${address[0].adminArea}")
        return address[0].getAddressLine(0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuAddToCalendar -> {
                if (marker != null) {
                    //addToCalendar(getString(R.string.icesatFlyover), pointChains[0][0].dateObject, pointChains[0][0].latitude, pointChains[0][0].longitude)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.selectALocation), Toast.LENGTH_LONG).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}