package gov.nasa.gsfc.icesat2.icesat_2

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

interface LatLngInterpolator {
    fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
        val lat = (b.latitude - a.latitude) * fraction + a.latitude
        var lngDelta = b.longitude - a.longitude

        // Take the shortest path across the 180th meridian.
        if (Math.abs(lngDelta) > 180) {
            lngDelta -= Math.signum(lngDelta) * 360
        }
        val lng = lngDelta * fraction + a.longitude
        return LatLng(lat, lng)
    }

}

private const val TAG = "SatelliteTrackingFrag"

private const val ZOOM_LEVEL = 1F

class SatelliteTrackingFragment : Fragment(), OnMapReadyCallback, LatLngInterpolator {

    private lateinit var mMap: GoogleMap
    //one point every five seconds
    //private val satellitePos = arrayOf(LatLng(39.3358, -76.9206), LatLng(39.0807, -76.952), LatLng(38.7169, -76.999))
    private val satellitePos = arrayOf(LatLng(-79.889, 0.2562), LatLng(-82.1362, -10.0522), LatLng(30.1362, -15.0522))
    private val timeIncrement = 5000L
    private lateinit var satelliteMarker: Marker
    private var continueAnimating = true
    private var count = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_satellite_tracking, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        val polylineOptions = PolylineOptions().geodesic(true)
        for (element in satellitePos) {
            polylineOptions.add(element)
        }
        mMap.addPolyline(polylineOptions)



        val iconBitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.icesatc)
        satelliteMarker = mMap.addMarker(MarkerOptions().position(satellitePos[0]).icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(satellitePos[0], ZOOM_LEVEL))
        animateMarkerToICS(satelliteMarker, satellitePos[count])

    }

    private fun animateMarkerToICS(marker: Marker, finalPosition: LatLng) {
        val typeEvaluator = object : TypeEvaluator<LatLng> {

            override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
                return interpolate(fraction, startValue, endValue);
            }
        }

        val property: Property<Marker, LatLng> = Property.of(Marker::class.java, LatLng::class.java, "position")
        val animator: ObjectAnimator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = timeIncrement
        animator.interpolator = LinearInterpolator()

        animator.start()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                if (continueAnimating && count + 1 < satellitePos.size) {
                    //move the camera
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(satellitePos[count], ZOOM_LEVEL))

                    count++
                    Log.d(TAG, "animation ends count is now $count")
                    animateMarkerToICS(satelliteMarker, satellitePos[count])
                }
            }

        })
    }

    override fun onStop() {
        super.onStop()
        continueAnimating = false
    }
}
