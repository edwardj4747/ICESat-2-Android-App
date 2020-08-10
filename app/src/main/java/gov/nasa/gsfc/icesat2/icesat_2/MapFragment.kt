package gov.nasa.gsfc.icesat2.icesat_2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlin.math.abs


private const val TAG = "MapFragment"
private const val T2 = "EdwardSecondTag"
private const val BUNDLE_MAP_OPTIONS = "BundleMapOptions"

class MapFragment : Fragment(), IShareAndCalendar, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, IMarkerSelectedCallback,
GoogleMap.OnPolylineClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var pointList: ArrayList<Point>
    private var pastFutureThreshold = 0
    private lateinit var pastPointList: ArrayList<Point>
    private lateinit var searchCenter: LatLng
    private var searchRadius: Double = -1.0
    private lateinit var fm: FragmentManager
    private lateinit var markerSelectedFragment: MarkerSelectedFragment
    private var marker: Marker? = null //used to keep track of the selected marker
    private var count = 0 //to access the point array based on the marker later
    private var markerList = ArrayList<Marker>()
    private val polylineList = ArrayList<Polyline>()
    private val laserBeamList = ArrayList<PolylineOptions>()
    private lateinit var laserPolylines: ArrayList<Polyline>
    private val flyoverDatesAndTimes = ArrayList<String>()
    private var markersPlotted = false //have the markers already been added to the map
    private val offsets = arrayOf(-3390, -3300, -47, 47, 3300, 3390) //for calculating the position of the laser beam
    private var bundleString: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivity created")

        setHasOptionsMenu(true)
        fm = childFragmentManager

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mainActivityViewModel = MainActivity.getMainViewModel()

        Log.d(TAG, "CHECKING PAST AND future values")
        Log.d(TAG, "size ${MainActivity.getMainViewModel()?.pastPointsList?.value?.size}")
        Log.d(TAG, "${MainActivity.getMainViewModel()?.allPointsList?.value?.size}")

        pointList = arrayListOf()
        if (mainActivityViewModel?.pastPointsList?.value != null) {
            pointList.addAll(mainActivityViewModel?.pastPointsList?.value!!)
            pastFutureThreshold = mainActivityViewModel?.pastPointsList?.value!!.size - 1
        }

        if (mainActivityViewModel?.allPointsList?.value != null) {
            pointList.addAll(mainActivityViewModel?.allPointsList?.value!!)
        }
        Log.d(TAG, "pointList size is ${pointList.size}")
        if (this::mMap.isInitialized) {
            addChainPolyline(pointList)
        }

        //check if there are both future and past points
        if (pastFutureThreshold != -1 && pastFutureThreshold + 1 < pointList.size) {
            //textViewPastFuture.visibility = View.VISIBLE
            constraintLayoutPastFuture.visibility = View.VISIBLE
        }

        /*mainActivityViewModel?.getAllPointsList()?.observe(viewLifecycleOwner, Observer {
            //if MapFragment was launched from MainActivity, we are guaranteed to have at least one result
            Log.d(TAG, "allPointsList is observed. Size of pointList is ${it.size}")
            val notificationTime = mainActivityViewModel.notificationTime.value
            Log.d(TAG, "notificationTime is $notificationTime")
            if (notificationTime!= null && notificationTime != -1L) {
                //search launched by clicking on notification. Only want to show the track with the notification
                pointList = ArrayList()
                textViewSeeAll.visibility = View.VISIBLE
                for (i in 0 until it.size) {
                    if (onSameChain(it[i], notificationTime)) {
                        pointList.add(it[i])
                    }
                }
            } else {
                //want to show all the points
                pointList = it
            }
            if (this::mMap.isInitialized && !markersPlotted && markerList.isNotEmpty()) {
                Log.d(TAG, "Adding polylines from inside observer")
                markersPlotted = true
                addChainPolyline(it)
            }
        })

        //todo check with notiications
        mainActivityViewModel?.getAllPastPointsList()?.observe(viewLifecycleOwner, Observer {
            pastPointList = it
            if (this::mMap.isInitialized) {
                Log.d(TAG, "Adding Past polylines from inside observer")
                markersPlotted = true
                addChainPolyline(pastPointList, false)
            } else {
                Log.d(TAG, "NOT Adding past points observer")
            }
        })*/

        textViewSeeAll.setOnClickListener {
            textViewSeeAll.visibility = View.INVISIBLE
            pointList = mainActivityViewModel?.allPointsList?.value!!
            //clear everything and redraw everything
            count = 0
            markerList.clear()
            polylineList.clear()
            Log.d(TAG, "markerLIst is ${markerList.size}")
            Log.d(TAG, "polyline list is ${polylineList.size}")

            laserBeamList.clear()
            flyoverDatesAndTimes.clear()
            addChainPolyline(pointList)
        }

        mainActivityViewModel?.getSearchCenter()?.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "MapFragment searchCenterObserved to be ${it.latitude}, ${it.longitude}")
            searchCenter = it
        })

        mainActivityViewModel?.getSearchRadius()?.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "MapFrag searchRadius observed to be $it")
            searchRadius = it
        })

        checkBoxMarker.setOnClickListener {
            if (checkBoxMarker.isChecked) {
                markerList.forEach {
                    it.isVisible = true
                }
            } else {
                markerList.forEach {
                    it.isVisible = false
                }
            }
        }

        checkBoxPath.setOnClickListener {
            if (checkBoxPath.isChecked) {
                polylineList.forEach {
                    it.isVisible = true
                }
            } else {
                polylineList.forEach {
                    it.isVisible = false
                }
            }
        }

        laserPolylines = ArrayList<Polyline>()


        checkBoxLasers.setOnClickListener {
            if (checkBoxLasers.isChecked && laserBeamList.isEmpty()) {
                calculateLaserBeams()
                populateLaserPolylines()
            }
            if (checkBoxLasers.isChecked) {
                laserPolylines.forEach {
                    it.isVisible = true
                }
            } else {
                laserPolylines.forEach {
                    it.isVisible = false
                }
            }
        }

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewstate restored called with $savedInstanceState")

        //if exists of form true, false, false for marker, tracks, laser beams
        bundleString = savedInstanceState?.getString(BUNDLE_MAP_OPTIONS)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady starts")
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        mMap.setOnPolylineClickListener(this)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }


        //future points
        if (this::pointList.isInitialized && !markersPlotted) {
            Log.d(TAG, "Adding polylines inside onMapReady")
            markersPlotted = true
            addChainPolyline(pointList)
        }

        /*//past points
        if (this::mMap.isInitialized) {
            Log.d(TAG, "Adding Past polylines from inside observer")
            markersPlotted = true
            addChainPolyline(pastPointList, false)
        } else {
            Log.d(TAG, "NOT Adding past points onMapReady")
        }*/
    }

    private fun addChainPolyline(chain: ArrayList<Point>) {
        Log.d(TAG, "addChainPolyLine Starts")
        var polylineTag = 0
        var polylineOptions = PolylineOptions()

        val pastColor = 0xff007fff.toInt()

        count = 0
        //adding a marker at each point - maybe polygons are a better way to do this
        val myMarker = MarkerOptions()
        for (i in 0 until chain.size) {
            //check if the point is on the same chain as the previous point
            if (i != 0 && !onSameChain(chain[i - 1], chain[i])) {
                if (count <= pastFutureThreshold + 1) {
                    drawPolyline(polylineOptions, polylineTag, pastColor)
                } else {
                    drawPolyline(polylineOptions, polylineTag)
                }
                polylineTag = count
                polylineOptions = PolylineOptions()
            }
            polylineOptions.add(LatLng(chain[i].latitude, chain[i].longitude))
            //add marker to map and markerList


            val markerAdded = mMap.addMarker(

                myMarker.position(LatLng(chain[i].latitude, chain[i].longitude))
                    .title(getString(R.string.latLngDisplayString, chain[i].latitude.toString(), 0x00B0.toChar(), chain[i].longitude.toString(), 0x00B0.toChar())))


            if (count <= pastFutureThreshold) {
                //markerAdded.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                markerAdded.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            } else {
                markerAdded.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            }

            markerAdded.tag = count
            markerList.add(markerAdded)
            count++
        }
        Log.d(TAG, "LAST TIME $count and thresh $pastFutureThreshold")
        if (count <= pastFutureThreshold + 1) { // +1 because count gets incremented before this check
            drawPolyline(polylineOptions, polylineTag, pastColor)
        } else {
            drawPolyline(polylineOptions, polylineTag)
        }
        addCircleRadius(searchRadius)

        correctForStateIfNeeded()
    }

    private fun correctForStateIfNeeded() {
        Log.d(T2, "Starting correct if neededbundleString is $bundleString")
        if (bundleString != null) {
            Log.d(T2, "string is $bundleString")
            try {
                val splitString = bundleString!!.split(",")
                val markers = splitString[0]
                val tracks = splitString[1]
                val lasers = splitString[2]

                checkBoxMarker.isChecked = markers == "true"
                Log.d(T2, "markers ${markers}")
                if (!checkBoxMarker.isChecked) {
                    Log.d(T2, "click marker checkbox")
                    markerList.forEach {
                        it.isVisible = false
                    }
                }
                checkBoxPath.isChecked = tracks == "true"
                Log.d(T2, "paths ${tracks}")
                if (!checkBoxPath.isChecked) {
                    Log.d(T2, "click path checkbox")
                    polylineList.forEach {
                        it.isVisible = false
                    }
                }
                checkBoxLasers.isChecked = lasers == "true"

                Log.d(T2, "lasers ${lasers}")
                if (checkBoxLasers.isChecked) {
                    if (laserPolylines.isEmpty()) {
                        calculateLaserBeams()
                        populateLaserPolylines()
                    }
                }

            } catch (e: Exception) {
                Log.d(T2, "caught exception ${e.message}")
            }
        }
    }

    private fun onSameChain(p1: Point, p2: Point): Boolean {
        val timingThreshold = 60
        //return p1.dateObject.time + timingThreshold * 1000 > p2.dateObject.time
        return abs(p1.dateObject.time - p2.dateObject.time) < timingThreshold * 1000
    }

    private fun onSameChain(p1: Point, time: Long): Boolean {
        val timingThreshold = 60
        return abs(p1.dateObject.time - time) < timingThreshold * 1000
    }

    private fun drawPolyline(polylineOptions: PolylineOptions, tagValue: Int, lineColor: Int = 0xff32CD32.toInt() ) {
        Log.d(TAG, "Adding polyline with tag $tagValue")
        polylineList.add(mMap.addPolyline(polylineOptions).apply {
            jointType = JointType.ROUND
            color = lineColor
            isClickable = true
            tag = tagValue
        })
        //add date to flyover date
        flyoverDatesAndTimes.add(
            String.format(
                "%s (%.5s %s)",
                pointList[tagValue].date,
                pointList[tagValue].time,
                pointList[tagValue].ampm
            )
        )
    }

    private fun addCircleRadius(radius: Double) {
        val milesToMeters = 1609.34
        val circleOptions = CircleOptions().radius(radius * milesToMeters).center(searchCenter)
        mMap.addCircle(circleOptions)
        //mMap.addMarker(MarkerOptions().position(LatLng(10.0, 10.0)))

        val circle = mMap.addCircle(circleOptions)

        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                circleOptions.center,
                getZoomLevel(circle)
            )
        )

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


    override fun onMarkerClick(p0: Marker?): Boolean {
        constraintLayoutPastFuture.visibility = View.INVISIBLE
        marker = p0
        val markerTag = marker?.tag as Int
        showMarkerDisplayFragment(markerTag, true)
        return false
    }

    private fun showMarkerDisplayFragment(tagValue: Int, isMarker: Boolean) {
        val fragmentTransaction = fm.beginTransaction()
        markerSelectedFragment = MarkerSelectedFragment.newInstance(pointList[tagValue], isMarker)
        fragmentTransaction.apply {
            setCustomAnimations(R.anim.slide_in_down, R.anim.blank_animation)
            replace(R.id.mapFragmentContainer, markerSelectedFragment)
            commit()
        }
    }

    override fun onMapClick(p0: LatLng?) {
        marker = null
        if (this::markerSelectedFragment.isInitialized) {
            fm.beginTransaction().apply {
                setCustomAnimations(R.anim.blank_animation, R.anim.slide_out_down)
                replace(R.id.mapFragmentContainer, DummyFragment())
                commit()
            }
            constraintLayoutPastFuture.visibility = View.VISIBLE
        }
    }

    override fun onPolylineClick(p0: Polyline?) {
        Log.d(
            TAG,
            "polylineClicked. Tag is ${p0?.tag} chain date is ${pointList[p0?.tag as Int].dateString}"
        )
        showMarkerDisplayFragment(p0.tag as Int, false)
    }

    override fun closeButtonPressed() {
        onMapClick(null)
    }

    private fun calculateLaserBeams() {
        Log.d(TAG, "Calculating laser beams")

        var laserBeamListIndex = -1 // will be incremented to zero on the first pass through the loop
        //calculating all the left sides

        for (element in offsets) {
            for (count in 0 until pointList.size) {
                val latN = pointList[count].latitude
                val long = pointList[count].longitude
                val newLong = degreesOfLong(element, latN)

                if (count == 0 || !onSameChain(pointList[count], pointList[count - 1])) {
                    //Log.d(TAG, "count = $count creating a new polyline for the laser beams")
                    laserBeamList.add(PolylineOptions())
                    laserBeamListIndex++
                }

                laserBeamList[laserBeamListIndex] =
                    laserBeamList[laserBeamListIndex].add(LatLng(latN, long + newLong))
            }
        }
    }

    private fun populateLaserPolylines() {
        val colorsArr = requireContext().resources.getStringArray(R.array.greenColors)
        val dash: PatternItem = Dash(30F)
        val gap: PatternItem = Gap(20F)
        val dashedPolyline: List<PatternItem> = listOf(gap, dash)

        val indo = laserBeamList.size / offsets.size //diving by the number of entries in offset
        Log.d(TAG, "indo is $indo. laserBeamLIst size ${laserBeamList.size} offsets.size = ${offsets.size}")
        if (this::mMap.isInitialized) {
            for (i in laserBeamList.indices) {
                if (indo < colorsArr.size) {
                    Log.d(TAG, "plotting color i % indo ${i % indo}")
                    laserPolylines.add(mMap.addPolyline(laserBeamList[i].color(parseColor(colorsArr[i % indo])).pattern(dashedPolyline)))
                } else {
                    Log.d(TAG, "plotting color sie - 1 :(")
                    laserPolylines.add(mMap.addPolyline(laserBeamList[i].color(parseColor(colorsArr[(i % indo) % colorsArr.size])).pattern(dashedPolyline)))
                }
            }
        }
    }


    private fun degreesOfLong(distance: Int, lat: Double): Double {
        return  distance / (kotlin.math.cos(Math.toRadians(lat)) * 111000)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuAddToCalendar -> {
                attemptToAddToCalendar()
            }
            R.id.menuShare -> {
                //method from IShare interface which shows the sharing dialog
                showShareScreen(mMap, requireActivity(), requireContext(), flyoverDatesAndTimes)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun attemptToAddToCalendar() {
        if (marker != null) {
            val markerTag = marker?.tag as Int
            Log.d(TAG, "markerTag is $markerTag; Point is ${pointList[markerTag]}")
            addToCalendar(requireContext(),
                getString(R.string.icesatFlyover),
                pointList[markerTag].dateObject.time,
                pointList[markerTag].latitude,
                pointList[markerTag].longitude
            )
        } else {
            Toast.makeText(requireContext(), getString(R.string.selectALocation), Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapBundleValues = "${checkBoxMarker.isChecked},${checkBoxPath.isChecked},${checkBoxLasers.isChecked}"
        Log.d(TAG, "adding to Bundle $mapBundleValues")
        outState.putString(BUNDLE_MAP_OPTIONS, mapBundleValues)
    }

}