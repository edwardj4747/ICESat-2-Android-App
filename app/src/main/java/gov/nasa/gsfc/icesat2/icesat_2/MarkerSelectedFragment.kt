package gov.nasa.gsfc.icesat2.icesat_2

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry
import gov.nasa.gsfc.icesat2.icesat_2.ui.favorites.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_marker_selected.*

private const val TAG = "MarkerSelectedFragment"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

/**
 * A simple [Fragment] subclass.
 * Use the [MarkerSelectedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MarkerSelectedFragment : Fragment(), IGeocoding {

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var selectedPoint: Point
    //one of these two will always be null because this is for one favorite entry and cannot both add and remove it
   /* private var favoritesEntryToAdd: FavoritesEntry? = null //null if no new favorite to add
    private var favoritesEntryToRemove: FavoritesEntry? = null //null if no favorite to remove*/
    private var favoritesEntryToAdd: Point? = null
    private var favoritesEntryToRemove: Point? = null
    private lateinit var geocoder: Geocoder
    private var isMarker: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedPoint = it.getParcelable<Point>(ARG_PARAM3)!!
            isMarker = it.getBoolean(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_marker_selected, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favoritesViewModel =
            ViewModelProviders.of(this).get(FavoritesViewModel::class.java)

        geocoder = Geocoder(context)

        if (isMarker) {
            //textViewDate.text = "${selectedPoint.dayOfWeek}, ${selectedPoint.date}, ${selectedPoint.year}"
            //textViewTime.text = "${selectedPoint.time} ${selectedPoint.ampm} ${selectedPoint.timezone}"
            //todo: not sure this is going to work in other localities
            textViewDate.text = getString(R.string.dateDisplay, selectedPoint.dayOfWeek, selectedPoint.date, selectedPoint.year)
            textViewTime.text = getString(R.string.timeDisplay, selectedPoint.time, selectedPoint.ampm, selectedPoint.timezone)

            //if (entryInDatabase(FavoritesEntry(selectedPoint.dateObject.time, selectedPoint.dateString, selectedPoint.latitude, selectedPoint.longitude))) {
            if (entryInDatabase(selectedPoint.dateObject.time)) {
                btnFavorite.setImageResource(R.drawable.ic_shaded_star_24)
                btnFavorite.tag = "favorite"
            }

            btnFavorite.setOnClickListener {
                if (btnFavorite.tag == "favorite") {
                    //remove from favorites
                    btnFavorite.setImageResource(R.drawable.ic_star_border_black_24dp)
                    btnFavorite.tag = "notFavorite"
                    Toast.makeText(requireContext(), "Removed From Favorites", Toast.LENGTH_SHORT)
                        .show()
                    favoritesEntryToAdd = null
                    favoritesEntryToRemove = selectedPoint
                } else {
                    btnFavorite.setImageResource(R.drawable.ic_shaded_star_24)
                    btnFavorite.tag = "favorite"
                    Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_SHORT)
                        .show()
                    favoritesEntryToAdd = selectedPoint
                    favoritesEntryToRemove = null
                }

            }

            btnNotify.setOnClickListener {
                Log.d(TAG, "notify button clicked")
                btnNotify.setImageResource(R.drawable.ic_baseline_notifications_active_24)

                val intent = Intent(requireContext(), NotificationBroadcast::class.java)
                val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, 0)

                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 8000, pendingIntent)
                //NotificationBroadcast.createNotification(requireContext())
            }

        } else {
            //just want to display a chain entry
            textViewDate.text = getString(R.string.trackBeginsAt, selectedPoint.date, selectedPoint.year)
            /*val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

            textViewDate.layoutParams = params*/
            textViewTime.visibility = View.GONE
            btnFavorite.visibility= View.INVISIBLE
        }

        btnClose.setOnClickListener {
            val listener = requireParentFragment() as MapFragment
            listener.closeButtonPressed()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param3: Point, isMarker: Boolean) =
            MarkerSelectedFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM3, param3)
                    putBoolean(ARG_PARAM4, isMarker)
                }
            }
    }

    /**
     * If the users starred a location, add it to favorites when this fragment gets closed
     */
    override fun onStop() {
        super.onStop()

        if (favoritesEntryToAdd != null) {
            //val geocodedString = Geocoding.getGeographicInfo(geocoder, selectedPoint.latitude, selectedPoint.longitude)
            val geocodedString = getGeographicInfo(geocoder, selectedPoint.latitude, selectedPoint.longitude)
            val addingFavorite = FavoritesEntry(selectedPoint.dateObject.time, selectedPoint.dateString, selectedPoint.latitude, selectedPoint.longitude, geocodedString)
            if (!entryInDatabase(addingFavorite)) {
                Log.d(TAG, "entry is NOT in favorites. Adding it")
                favoritesViewModel.insert(addingFavorite)
            }
        } else if (favoritesEntryToRemove != null) {
            Log.d(TAG, "Entry was previously entered in favorites. Now removing it")
            //favoritesViewModel.delete(favoritesEntryToRemove!!.dateObjectTime)
            //favoritesViewModel.delete(FavoritesEntry(selectedPoint.dateObject.time, selectedPoint.dateString, selectedPoint.latitude, selectedPoint.longitude).dateObjectTime)
            favoritesViewModel.delete(selectedPoint.dateObject.time)
        }

    }

    private fun entryInDatabase(favEntry: FavoritesEntry) : Boolean {
            return favoritesViewModel.contains(favEntry.dateObjectTime)
    }

    private fun entryInDatabase(dateTime: Long) : Boolean {
        return favoritesViewModel.contains(dateTime)
    }
}