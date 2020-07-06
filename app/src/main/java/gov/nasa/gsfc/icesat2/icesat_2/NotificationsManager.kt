package gov.nasa.gsfc.icesat2.icesat_2

import android.app.Activity
import android.content.Context
import android.util.Log

//todo: if package name changes, have to change this too
private const val NOTIFICATION_SHARED_PREF = "gov.nasa.gsfc.icesat2.icesat_2.NotificationSharedPref"
private const val TAG = "NotificationsManager"


class NotificationsManager(activity: Activity) {

    private val sharedPreferences = activity.getSharedPreferences(NOTIFICATION_SHARED_PREF, Context.MODE_PRIVATE)

    fun addToNotificationSharedPref(timestamp: Long) {
        Log.d(TAG, "Adding $timestamp to sharedPref")
        with(sharedPreferences.edit()) {
            putLong(timestamp.toString(), timestamp)
            apply()
        }
    }

    fun removeFromSharedPref(timestamp: Long) {
        with(sharedPreferences.edit()) {
            try {
                remove(timestamp.toString())
                apply()
            } catch (e: Exception) {
                Log.d(TAG, "element not in sharedPreferences")
            }
        }
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