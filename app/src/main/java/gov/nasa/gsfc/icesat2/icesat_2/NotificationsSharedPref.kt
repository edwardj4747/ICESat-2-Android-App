package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.util.Log

//todo: if package name changes, have to change this too
private const val NOTIFICATION_SHARED_PREF = "gov.nasa.gsfc.icesat2.icesat_2.NotificationSharedPref"
private const val TAG = "NotificationsSharedPref"


class NotificationsSharedPref(context: Context) {

    //private val sharedPreferences = activity.getSharedPreferences(NOTIFICATION_SHARED_PREF, Context.MODE_PRIVATE)
    private val sharedPreferences = context.getSharedPreferences(NOTIFICATION_SHARED_PREF, Context.MODE_PRIVATE)

    fun addToNotificationSharedPref(timestamp: Long) {
        Log.d(TAG, "Adding $timestamp to sharedPref")
        with(sharedPreferences.edit()) {
            putLong(timestamp.toString(), timestamp)
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
        Log.d(TAG, "print all called")
        val allEntries = sharedPreferences.all
        Log.d(TAG, "allEntriesSize is ${allEntries.size}")
        for (element in allEntries.keys) {
            Log.d(TAG, "key is $element")
        }
        for ((key, value) in allEntries) {
            Log.d("map values", key + ": " + value.toString())
        }
    }

    fun getSharedPrefValues() = sharedPreferences.all.values

}