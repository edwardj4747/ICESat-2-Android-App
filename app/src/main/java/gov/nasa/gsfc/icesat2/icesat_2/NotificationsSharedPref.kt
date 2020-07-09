package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.util.Log

//todo: if package name changes, have to change this too
private const val NOTIFICATION_SHARED_PREF = "gov.nasa.gsfc.icesat2.icesat_2.NotificationSharedPref"
private const val TAG = "NotificationsSharedPref"


class NotificationsSharedPref(context: Context) {

    //private val sharedPreferences = activity.getSharedPreferences(NOTIFICATION_SHARED_PREF, Context.MODE_PRIVATE)
    private val sharedPreferences = context.getSharedPreferences(NOTIFICATION_SHARED_PREF, Context.MODE_PRIVATE)

    /**
     * 2nd param is string of
     * timeStampOfAlarm, lat, long, timeString
     */
    fun addToNotificationSharedPref(timestampOfFlyover: Long, notificationInfoString: String) {
        Log.d(TAG, "Adding $timestampOfFlyover to sharedPref")
        with(sharedPreferences.edit()) {
            putString(timestampOfFlyover.toString(), notificationInfoString)
            apply()
        }
    }




    fun addToNotificationSharedPref(timestampOfFlyover: Long, timeStampOfAlarm: Long) {
        Log.d(TAG, "Adding $timestampOfFlyover to sharedPref")
        with(sharedPreferences.edit()) {
            putLong(timestampOfFlyover.toString(), timeStampOfAlarm)
            apply()
        }
    }

    fun delete(timestamp: Long) {
        with(sharedPreferences.edit()) {
            try {
                remove(timestamp.toString())
                apply()
            } catch (e: Exception) {
                Log.d(TAG, "element not in sharedPreferences")
            }
        }
    }

    fun contains(timestamp: String): Boolean {
        return sharedPreferences.contains(timestamp)
    }

    fun deleteAll() {
        sharedPreferences.edit().clear().apply()
    }

    fun printAll() {
        Log.d(TAG, "print all keys called")
        val allEntries = sharedPreferences.all
        val keys = allEntries.keys
        Log.d(TAG, "allEntriesSize is ${allEntries.size}")
        for (element in keys) {
            Log.d(TAG, "key is: $element value is: ${sharedPreferences.getString(element, "Unknown??")}")
        }
    }

    fun getSharedPrefValues() = sharedPreferences.all.values

    fun getSharedPrefKeys() = sharedPreferences.all.keys

    fun get(key: String) = sharedPreferences.getString(key, "")

}