package gov.nasa.gsfc.icesat2.icesat_2

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val INTENT_TIME_REQUEST_CODE = "IntentRequestCode"
const val INTENT_LAT_LNG_STRING = "IntentLatLngString"
const val INTENT_TIME_STRING = "IntentTimeString"
const val NOTIFICATION_LAUNCHED_MAIN_ACTIVITY = "NotificationLaunchedMainActivity"
const val NOTIFICATION_LAT = "NotificationLat"
const val NOTIFICATION_LONG = "NotificationLong"
private const val CHANNEL_ID = "NotificationsTest"
private const val DESCRIPTION = "lorem ipsum de description foes here"
var notificationId = 1

private const val TAG = "NotificationBroadcast"

class NotificationBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive Called")

        val nm = NotificationsSharedPref(context!!)

        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            Log.d(TAG, "onBootReceived")
            /*val mServiceIntent = Intent(context, BootService::class.java)
            context?.startService(mServiceIntent)*/

            Log.d(TAG, "the values in sharedPreferences are----------")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val myIntent = Intent(context, NotificationBroadcast::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0)

            val notificationValues = nm.getSharedPrefValues()
            notificationValues.forEach {
                Log.d(TAG, "in shared pref $it")
                alarmManager.set(AlarmManager.RTC_WAKEUP, it as Long, pendingIntent)
            }


            /*if (context != null) {
                createNotification(context)
            }*/
        }



        val requestCodeToDelete = intent?.getLongExtra(INTENT_TIME_REQUEST_CODE, -1)
        val latLngString = intent?.getStringExtra(INTENT_LAT_LNG_STRING)
        val splitLatLngString = latLngString?.split(",")

        val latParam = splitLatLngString?.get(0)?.toDouble()
        val longParam = splitLatLngString?.get(1)?.toDouble()

        val time = intent?.getStringExtra(INTENT_TIME_STRING)

        if (context != null) {
            createNotification(context, latParam, longParam, time)
        }

        Log.d(TAG, "--------------------------")
        Log.d(TAG, "onReceive intent request code is${requestCodeToDelete}.}")

        //delete the notification with the request code passed in the intent
        nm.delete(requestCodeToDelete!!)

        Log.d(TAG, "After deleting $requestCodeToDelete; all values are")
        nm.printAll()


    }

    companion object {
        fun createNotification(context: Context, lat: Double?, long: Double?, time: String?) {

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.channel_name)
                val descriptionText = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            // Create an explicit intent for an Activity in your app
            val intent = Intent(context, MainActivity::class.java).apply {
                //todo: pass in real values for these
                if (lat != null && long != null) {
                    putExtra(NOTIFICATION_LAT, lat)
                    putExtra(NOTIFICATION_LONG, long)
                    putExtra(NOTIFICATION_LAUNCHED_MAIN_ACTIVITY, true)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val infoMessage = if (time == null) {
                context.getString(R.string.flyoverNotification)
            } else {
                context.getString(R.string.flyoverNotification) + " at $time"
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(context.getString(R.string.icesatFlyover))
                .setContentText(infoMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
            notificationId++
        }
    }

}

class BootService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

}