package gov.nasa.gsfc.icesat2.icesat_2

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.os.Bundle
import android.util.Log
import android.util.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_satellite_tracking.*
import kotlinx.coroutines.*
import java.net.URL

private const val TAG = "SatelliteTrackingFrag"

private const val ZOOM_LEVEL = 1F

class SatelliteTrackingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    //one point every five seconds
    //private val satellitePos = arrayOf(LatLng(39.3358, -76.9206), LatLng(39.0807, -76.952), LatLng(38.7169, -76.999))
    //private val satellitePos = arrayOf(LatLng(-79.889, 0.2562), LatLng(-82.1362, -10.0522), LatLng(30.1362, -15.0522))
    private lateinit var satellitePos: ArrayList<TrackingPoint>
    private val timeIncrement = 5000L
    private lateinit var satelliteMarker: Marker
    private var continueAnimating = true
    private var count = 0
    private lateinit var animator: ObjectAnimator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_satellite_tracking, container, false)
    }

    //todo: correct position among index 0 and index 1 points
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d(TAG, "onActivityCreated Starts")
        MainActivity.getMainViewModel()?.getTrackingData()?.observe(viewLifecycleOwner, Observer {
            //satellitePos = arrayListOf(it[0], it[1])
            //Log.d(TAG, "FOR THIS TEST using satellite pos as \n $satellitePos")
            satellitePos = it
            Log.d(TAG, "satellitePos.size is ${satellitePos.size}")
        })

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnMoreData.setOnClickListener {
            downloadMoreData()
        }

    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        drawSatellitePosPolyline()

        val currentTimeInMillis = System.currentTimeMillis()
        Log.d(TAG, "===================")
        Log.d(TAG, "start: ${satellitePos[0].timeInMillis}")
        Log.d(TAG, "curr : $currentTimeInMillis")
        Log.d(TAG, "end 1: ${satellitePos[1].timeInMillis}")
        Log.d(TAG, "===================")

        //LatLng and Double are the only types that will ever be put in the array, so casts are safe
        var calculatePositionArray = calculateStartingPosition(count)
        var startingPosition = calculatePositionArray[0] as LatLng
        var percentage = calculatePositionArray[1] as Double

        while (percentage > 1) {
            Log.d(TAG, "While loop running with percentage $percentage and count $count")
            count++
            calculatePositionArray = calculateStartingPosition(count)
            startingPosition = calculatePositionArray[0] as LatLng
            percentage = calculatePositionArray[1] as Double
        }

        val durationForFirstSegment = calculateTimeIncrement(0) * (1 - percentage)
        Log.d(TAG, "duration for first segment is ${durationForFirstSegment.toLong()}")

        //val startingPosition = LatLng(satellitePos[0].lat, satellitePos[0].long)
        satelliteMarker = mMap.addMarker(MarkerOptions().position(startingPosition).icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPosition, ZOOM_LEVEL))

        animateMarkerToICS(satelliteMarker, count, durationForFirstSegment.toLong())

    }

    private fun drawSatellitePosPolyline(dummyArr: ArrayList<TrackingPoint> = ArrayList()) {
        CoroutineScope(Dispatchers.Main).launch {
            val polylineOptions = PolylineOptions().geodesic(true)
            for (element in satellitePos) {
                polylineOptions.add(LatLng(element.lat, element.long))
            }
            mMap.addPolyline(polylineOptions)
        }
    }

    /**
     * @return an array of (LatLng, percentage) where LatLng is the starting LatLng and percentage is
     * an approximation of how much of the distance between the the first LatLng (before now) and the
     * second LatLng(after now) the satelitte has covered
     */
    private fun calculateStartingPosition(startingIndex: Int) : Array<Any> {
        val currentTimeInMillis = System.currentTimeMillis()
        val numerator = currentTimeInMillis - satellitePos[startingIndex].timeInMillis
        val denominator = satellitePos[startingIndex + 1].timeInMillis - satellitePos[startingIndex].timeInMillis
        val percentage = (numerator.toDouble() / denominator).toDouble()
        Log.d(TAG, "num $numerator denom $denominator percentage is $percentage")

        val dlat = satellitePos[startingIndex + 1].lat - satellitePos[startingIndex].lat
        val dlong = satellitePos[startingIndex + 1].long - satellitePos[startingIndex].long

        val startLat = satellitePos[startingIndex].lat + percentage * dlat
        val startLong = satellitePos[startingIndex].long + percentage * dlong
        Log.d(TAG, "dlat $dlat; dlong $dlong")
        Log.d(TAG, "beginLat ${satellitePos[startingIndex].lat} endLat ${satellitePos[startingIndex + 1].lat}, resLat $startLat")
        Log.d(TAG, "beginLong ${satellitePos[startingIndex].long} endlong ${satellitePos[startingIndex + 1].long}, resLat $startLong")

        //TODO: check for when crosses over from -88 to 88 or 180 degrees
        return arrayOf(LatLng(startLat, startLong), percentage)
    }

    private fun animateMarkerToICS(marker: Marker, finalMarkerCount: Int, duration: Long) {

        val finalPosition = LatLng(satellitePos[finalMarkerCount + 1].lat, satellitePos[finalMarkerCount + 1].long)
        val typeEvaluator = object : TypeEvaluator<LatLng> {

            override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
                return interpolate(fraction, startValue, endValue);
            }
        }

        val property: Property<Marker, LatLng> = Property.of(Marker::class.java, LatLng::class.java, "position")
        animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = duration
        animator.interpolator = LinearInterpolator()

        animator.start()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                Log.d(TAG, "reached ${satellitePos[count + 1].lat}, ${satellitePos[count + 1].long} at ${System.currentTimeMillis()}. diff is ${System.currentTimeMillis() - satellitePos[count + 1].timeInMillis}")
                if (continueAnimating && count + 2 < satellitePos.size) {
                    //move the camera
                    count++
                    //val newPosition = LatLng(satellitePos[count].lat, satellitePos[count].long)
                    Log.d(TAG, "animation ends count is now $count")
                    animateMarkerToICS(satelliteMarker, count, calculateTimeIncrement(count))

                    if (count > satellitePos.size - 3) {
                        downloadMoreData()
                    }
                } else {
                    Log.d(TAG, "all trackingData has been animated and shown")
                }
            }

        })
    }

    private fun calculateTimeIncrement(startingIndex: Int): Long {
        return satellitePos[count + 1].timeInMillis - satellitePos[startingIndex].timeInMillis
    }

    fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
        val lat = (b.latitude - a.latitude) * fraction + a.latitude
        var lngDelta = b.longitude - a.longitude

        // Take the shortest path across the 180th meridian.
        if (Math.abs(lngDelta) > 180) {
            lngDelta -= Math.signum(lngDelta) * 360
        }
        val lng = lngDelta * fraction + a.longitude
        try {
            textViewDisplayCoords.text = String.format("Lat: %.2f\nLon: %.2f", lat, lng)
        } catch (e: Exception) { Log.d(TAG, "set text CATCH BLOCK") }
        return LatLng(lat, lng)
    }

    fun downloadMoreData() {
        Log.d(TAG, "Starting Download of newData")
        val downloadData = DownloadData()
        //val currentTimeInMillis = System.currentTimeMillis()
        val lastTimeInCurrentData = satellitePos[satellitePos.size - 1].timeInMillis + 1
        val numResults = 30
        val downloadLink = "http://iwantthistoworkplease-env.eba-hrx22muq.us-east-1.elasticbeanstalk.com/find?time=$lastTimeInCurrentData&numResults=$numResults"
        //0th entry will be the same but all of the following will be different so we can just append the different ones
        try {
            val url = URL(downloadLink)
            CoroutineScope(Dispatchers.IO).launch {
                val newData: Deferred<ArrayList<TrackingPoint>> = async {
                    downloadData.downloadTrackingData(url)
                }

                if (newData.await().isNotEmpty()) {
                    Log.d(TAG, "previous size of satellitePos is ${satellitePos.size}")
                    for (i in 1 until newData.await().size) {
                        satellitePos.add(newData.await()[i])
                    }
                    Log.d(TAG, "new size of satellitePos is ${satellitePos.size}")
                    //remove the duplicates

                    /*Log.d(TAG, "Adding more data to satelliteTrack")
                    //this will probably return a few entries, so we need to check and remove the duplicates

                    newData.await()
                    //extend polyline too
                    Log.d(TAG, "Before adding new Points $satellitePos")
                    for (i in 2 until newData.await().size) {
                        satellitePos.add(newData.await()[i])
                    }
                    Log.d(TAG, "after adding new points $satellitePos")
                    Log.d(TAG, "new size of satellitePos = ${satellitePos.size}")
                    Log.d(TAG, "New Data added is ${newData.await()}")*/
                    Log.d(TAG, "about to draw polyline")
                    drawSatellitePosPolyline(newData.await())
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception trying to download more data")
        }
    }

    override fun onStop() {
        super.onStop()
        continueAnimating = false
        Log.d(TAG, "cancelling animator")
        animator.cancel()
    }
}
