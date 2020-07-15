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
    private lateinit var notifSharedPref: NotificationsSharedPref
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
        notifSharedPref = NotificationsSharedPref(requireContext())
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

            val selectedPointTime = selectedPoint.dateObject.time
            //if there is already a notification for this time && notification is in future (occasionally they don't display and thus don't delete); display the filled in icon
            if ((notifSharedPref.contains("${selectedPointTime}_1") && notifSharedPref.get("${selectedPointTime}_1")?.split(",")?.get(0)?.toLong()!! - System.currentTimeMillis() > 0L)
                || (notifSharedPref.contains("${selectedPointTime}_24") && notifSharedPref.get("${selectedPointTime}_24")?.split(",")?.get(0)?.toLong()!! - System.currentTimeMillis() > 0L)) {
                btnNotify.setImageResource(R.drawable.ic_baseline_notifications_active_24)
            } else if (notifSharedPref.contains("${selectedPointTime}_1") || notifSharedPref.contains("${selectedPointTime}_24")) {
                //notification there but already passed
                deleteNotificationFromSPAndAlarmMangager(arrayOf("${selectedPoint.dateObject.time}_1", "${selectedPoint.dateObject.time}_24"))
            }

            Log.d(TAG, "OnActivity Created notifications are")
            notifSharedPref.printAll()

            btnNotify.setOnClickListener {
                Log.d(TAG, "notify button clicked")


                //create dialog
                if (notifSharedPref.contains("${selectedPointTime}_1") || notifSharedPref.contains("${selectedPointTime}_24") || notifSharedPref.contains("${selectedPointTime}_C")) {
                    //removing notifications
                    btnNotify.setImageResource(R.drawable.ic_baseline_notifications_none_24)
                    deleteNotificationFromSPAndAlarmMangager(arrayOf("$selectedPointTime" + "_24", "$selectedPointTime" + "_1", "${selectedPointTime}_C")) //the keys of the 24hr, 1hr, and custom alarm
                    Toast.makeText(context, "Notifcations Removed", Toast.LENGTH_SHORT).show()
                } else {
                    val notificationsDialog = NotificationsDialog()
                    notificationsDialog.setListener(this) //when the user presses ok on the dialog will call notificationsOptionsChosen
                    notificationsDialog.show(childFragmentManager, "hello world")
                }

                return@setOnClickListener




                if (notifSharedPref.contains(selectedPointTime.toString())) {
                    btnNotify.setImageResource(R.drawable.ic_baseline_notifications_none_24)
                    deleteNotificationFromSPAndAlarmMangager(arrayOf("$selectedPointTime" + "_24", "$selectedPointTime" + "_1")) //the keys of the 24hr and 1hr alarm
                    Toast.makeText(requireContext(), "Notification Cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    btnNotify.setImageResource(R.drawable.ic_baseline_notifications_active_24)
                    //launch the date picker
                    /*val calendar = getCalendarForSelectedPoint()
                    //calendar.timeZone = TimeZone.getTimeZone("UTC")
                    val datePickerFragment = DatePickerFragment(requireActivity())
                    datePickerFragment.setListener(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    datePickerFragment.show(childFragmentManager, "DatePicker")*/

                    //set notifications for 24 hrs before and 1hr before

                    createAlarm(selectedPointTime - 24 * 60 * 60 * 1000 - 60000, "$selectedPointTime" + "_24") //24hrs in advance
                    createAlarm(selectedPointTime - 60 * 60 * 1000 - 60000, "$selectedPointTime" + "_1") //1 hr in advance
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

    fun notificationOptionsChosen(arr: ArrayList<Int>) {
        //todo: remove this after testing
        Log.d(TAG, "Deleting all previous notifications")
        notifSharedPref.deleteAll()
        Log.d(TAG, "notification options chosen callback")

        btnNotify.setImageResource(R.drawable.ic_baseline_notifications_active_24)


        // 0 -> 1 hrs; 1 -> 24hrs; 2 -> set custom
        val flyoverTime = selectedPoint.dateObject.time
        val baseTimeKey = flyoverTime.toString()
        val currentTime = System.currentTimeMillis()
        for (element in arr) {
            var key = baseTimeKey
            Log.d(TAG, "Element: $element")
            when (element) {
                0 -> {
                    key += "_1"
                    createAlarm(currentTime + 100000, key)
                    //createAlarm(flyoverTime - 60 * 60 * 1000, key)
                }
                1 -> {
                    key += "_24"
                    createAlarm(currentTime + 90000, key)
                    //createAlarm(flyoverTime - 24 * 60 * 60 * 1000, key)
                }
                2 -> {
                    key += "_C"
                    //launch the date picker
                    val calendar = getCalendarForSelectedPoint()
                    //calendar.timeZone = TimeZone.getTimeZone("UTC")
                    val datePickerFragment = DatePickerFragment(requireActivity())
                    datePickerFragment.setListener(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    datePickerFragment.show(childFragmentManager, "DatePicker")
                }
            }
            Log.d(TAG, "create alrarm with key $key")
        }
        Log.d(TAG, "After onNotificationReceivedLoop")
        notifSharedPref.printAll()
    }

    private fun deleteNotificationFromSPAndAlarmMangager(arraySelectedPointTime: Array<String>) {
        Log.d(TAG, "Delete SP size of arry is ${arraySelectedPointTime.size}")
        //remove the notification from storage in SharedPreferences
        for (string in arraySelectedPointTime) {
            notifSharedPref.delete(string)
            //actually cancel the alarm
            //create a pending intent with the same properties
            Log.d(TAG, "Attempting to cancel a pending intent $string")
            val intent = Intent(requireContext(), NotificationBroadcast::class.java)
            val pendingIntent = PendingIntent.getBroadcast(requireContext(), string.hashCode(), intent, 0)
            pendingIntent.cancel()
            alarmManager.cancel(pendingIntent)
        }
        Log.d(TAG, "after deleting resulting notifications are")
        notifSharedPref.printAll()

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
        Log.d(TAG, "Time has been picked")
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        //calendar.timeZone = TimeZone.getTimeZone("UTC")
        val time = calendar.timeInMillis
        val key = "${selectedPoint.dateObject.time}_C"
        createAlarm(time, key)
        Log.d(TAG, "Creating Custom alarm with key $key")
        //Toast.makeText(requireContext(), "Notification Set", Toast.LENGTH_SHORT).show()
    }

    /**
     * Does two things
     * 1) creates a pendingIntent to call [NotificationBroadcast] when alarm is triggered with, notification
     * specific extras (lat, long, time...etc)
     * 2) stores the details of the alarm in sharedPreferences, so the alarm can be recreated after device turns back on
     * Alarms are stored in shared preferences using the following format
     * key: timeOfFlyover; value: timeForAlarm, lat, long, timeString, searchString, dateString
     *
     * @param timeForAlarm when the alarm will go off
     * @param timeForKey the time of the flyover (can be the same, but almost always timeForAlarm will be first)
     */
    private fun createAlarm(timeForAlarm: Long, timeForKey: String) {
        val intent = Intent(requireContext(), NotificationBroadcast::class.java)

        val latLngString = "${selectedPoint.latitude}, ${selectedPoint.longitude}"
        val timeString = "${selectedPoint.time.substring(0,5)} ${selectedPoint.ampm} ${selectedPoint.timezone}"
        val searchString = MainActivity.getMainViewModel()?.searchString?.value
        val dateString = selectedPoint.date
        val hours = timeForKey.split("_")[1]
        //add the values as extras to the intent
        intent.putExtra(INTENT_FLYOVER_TIME_KEY, timeForKey) //flyoverTime key
        intent.putExtra(INTENT_LAT_LNG_STRING, latLngString)
        intent.putExtra(INTENT_TIME_STRING, timeString)
        intent.putExtra(INTENT_SEARCH_STRING, searchString)
        intent.putExtra(INTENT_DATE_STRING, dateString)
        intent.putExtra(INTENT_HOURS_REMINDER, hours)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), timeForKey.hashCode(), intent, 0)
        //Log.d(TAG, "PendingIntent hashcode is ${timeForKey.hashCode()}")

        //1) add to the list of alarms with a formattedString of format timeStampOfAlarm, lat, long, timeString, searchString, dateString
        notifSharedPref.addToNotificationSharedPref(timeForKey, "$timeForAlarm, $latLngString, $timeString, $searchString, $dateString")
        //2) set the alarm. Alarms tend to run a little late, so show them 1 minute (60000ms) before
        Log.d(TAG, "alarm set to go off in ${(timeForAlarm - System.currentTimeMillis()) / 1000}s")
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeForAlarm - 60000, pendingIntent)
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