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
import java.util.*

private const val TAG = "MarkerSelectedFragment"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

/**
 * A simple [Fragment] subclass.
 * Use the [MarkerSelectedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MarkerSelectedFragment : Fragment(), IGeocoding, ITimePickerCallback {

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var selectedPoint: Point
    //one of these two will always be null because this is for one favorite entry and cannot both add and remove it
   /* private var favoritesEntryToAdd: FavoritesEntry? = null //null if no new favorite to add
    private var favoritesEntryToRemove: FavoritesEntry? = null //null if no favorite to remove*/
    private var favoritesEntryToAdd: Point? = null
    private var favoritesEntryToRemove: Point? = null
    private lateinit var geocoder: Geocoder
    private var isMarker: Boolean = true
    private lateinit var notificationsSharedPref: NotificationsSharedPref
    private lateinit var alarmManager: AlarmManager
    private val timeBeforeAlert = 8 * 60 * 60 * 1000 //will be used to display the notification eight hours before


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
        notificationsSharedPref = NotificationsSharedPref(requireContext())
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager


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

            //if there is already a notification for this time && notification is in future (occasionally they don't display and thus don't delete); display the filled in icon
            if (notificationsSharedPref.contains(selectedPoint.dateObject.time.toString())
                && notificationsSharedPref.get(selectedPoint.dateObject.time.toString())?.split(",")?.get(0)?.toLong()!! - System.currentTimeMillis() > 0L) {
                btnNotify.setImageResource(R.drawable.ic_baseline_notifications_active_24)
                Log.d(TAG, "second condition ${notificationsSharedPref.get(selectedPoint.dateObject.time.toString())?.split(",")?.get(0)?.toLong()}")
                Log.d(TAG, "current time     ${System.currentTimeMillis()}")
                Log.d(TAG, "subtract ${notificationsSharedPref.get(selectedPoint.dateObject.time.toString())?.split(",")?.get(0)?.toLong()!! - System.currentTimeMillis()}")
            } else if (notificationsSharedPref.contains(selectedPoint.dateObject.time.toString())) {
                //notification there but already passed
                deleteNotificationFromSPAndAlarmMangager(selectedPoint.dateObject.time)
            }

            Log.d(TAG, "SelectedPointTime si ${selectedPoint.dateObject.time}")
            Log.d(TAG, "OnActivity Created notifications are")
            notificationsSharedPref.printAll()

            btnNotify.setOnClickListener {
                Log.d(TAG, "notify button clicked")
                val selectedPointTime = selectedPoint.dateObject.time

                //val selectedPointTime = selectedPoint.dateObject.time

                if (notificationsSharedPref.contains(selectedPointTime.toString())) {
                    btnNotify.setImageResource(R.drawable.ic_baseline_notifications_none_24)
                    deleteNotificationFromSPAndAlarmMangager(selectedPointTime)
                    Toast.makeText(requireContext(), "Notification Cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    btnNotify.setImageResource(R.drawable.ic_baseline_notifications_active_24)
                    //launch the date picker
                    val calendar = getCalendarForSelectedPoint()
                    //calendar.timeZone = TimeZone.getTimeZone("UTC")
                    val datePickerFragment = DatePickerFragment(requireActivity())
                    datePickerFragment.setListener(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    datePickerFragment.show(childFragmentManager, "DatePicker")
                }


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

    private fun deleteNotificationFromSPAndAlarmMangager(selectedPointTime: Long) {
        //remove the notification from storage in SharedPreferences
        notificationsSharedPref.delete(selectedPointTime)
        Log.d(TAG, "after deleting resulting notifications are")
        notificationsSharedPref.printAll()
        //actually cancel the alarm
        //create a pending intent with the same properties
        Log.d(TAG, "Attempting to cancel a pending intent")
        val intent = Intent(requireContext(), NotificationBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), selectedPointTime.hashCode(), intent, 0)
        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
    }

    private fun getCalendarForSelectedPoint(): Calendar {
        val selectedPointTime = selectedPoint.dateObject.time
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedPointTime
        return calendar
    }

    /**
     * called when a date has been selected from the date picker.
     * @param year, month, day are the respective year, month, and day choosen from the datepicker
     */
    override fun datePicked(year: Int, month: Int, day: Int) {
        val calendar = getCalendarForSelectedPoint()
        calendar.timeZone = TimeZone.getDefault()

        //launch the timer picker
        val timePickerFragment = TimePickerFragment(requireActivity())
        timePickerFragment.setListener(this, year, month, day, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        timePickerFragment.show(childFragmentManager, "My Message")
    }

    /**
     * Called once a time has been picked. Create an alarm to go off at the chosen time
     */
    override fun timePicked(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        //calendar.timeZone = TimeZone.getTimeZone("UTC")
        val time = calendar.timeInMillis
        createAlarm(time, selectedPoint.dateObject.time)
        Log.d(TAG, "Creating alarm at $time")
        Toast.makeText(requireContext(), "Notification Set", Toast.LENGTH_SHORT).show()
    }

    /**
     * Does two things
     * 1) creates a pendingIntent to call [NotificationBroadcast] when alarm is triggered with, notification
     * specific extras (lat, long, time...etc)
     * 2) stores the details of the alarm in sharedPreferences, so the alarm can be recreated after device turns back on
     * Alarms are stored in shared preferences using the following format
     * key: timeOfFlyover; value: timeForAlarm, lat, long, timeString
     *
     * @param timeForAlarm when the alarm will go off
     * @param timeForKey the time of the flyover (can be the same, but almost always timeForAlarm will be first)
     */
    private fun createAlarm(timeForAlarm: Long, timeForKey: Long) {
        val intent = Intent(requireContext(), NotificationBroadcast::class.java)
        //adding the request code to the intent, so that we can delete it after we show it
        intent.putExtra(INTENT_TIME_REQUEST_CODE, timeForKey)
        intent.putExtra(INTENT_LAT_LNG_STRING, "${selectedPoint.latitude}, ${selectedPoint.longitude}")
        intent.putExtra(INTENT_TIME_STRING, "${selectedPoint.time.substring(0,5)} ${selectedPoint.ampm}")
        //I think? this is the same as INTENT_FLYOVER_TIME
        intent.putExtra(INTENT_FLYOVER_TIME, selectedPoint.dateObject.time)
        Log.d(TAG, "haschode is ${timeForKey.hashCode()}")
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), timeForKey.hashCode(), intent, 0)


        //1) add to the list of alarms with a fancy formattedString of format timeStampOfAlarm, lat, long, timeString
        //notificationsSharedPref.addToNotificationSharedPref(timeForKey, timeForAlarm)
        notificationsSharedPref.addToNotificationSharedPref(timeForKey, "$timeForAlarm, ${selectedPoint.latitude}, ${selectedPoint.longitude}, ${selectedPoint.time.substring(0,5)} ${selectedPoint.ampm}")
        //2) set the alarm
        Log.d(TAG, "alarm set to go off in ${(timeForAlarm - System.currentTimeMillis()) / 1000}s")
        /*//show the alert about 8 hrs
        alarmManager.set(AlarmManager.RTC_WAKEUP, time - timeBeforeAlert, pendingIntent)*/
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeForAlarm, pendingIntent)

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