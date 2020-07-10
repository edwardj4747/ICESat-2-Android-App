package gov.nasa.gsfc.icesat2.icesat_2

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val INTENT_TIME_REQUEST_CODE = "IntentRequestCode"
const val INTENT_LAT_LNG_STRING = "IntentLatLngString"
const val INTENT_TIME_STRING = "IntentTimeString"
const val INTENT_FLYOVER_TIME = "IntentTime"
const val INTENT_SEARCH_STRING = "IntentSearchString"
const val NOTIFICATION_LAUNCHED_MAIN_ACTIVITY = "NotificationLaunchedMainActivity"
const val NOTIFICATION_LAT = "NotificationLat"
const val NOTIFICATION_LONG = "NotificationLong"
const val NOTIFICATION_TIME = "NotificationTime"
private const val CHANNEL_ID = "NotificationsTest"
private const val DESCRIPTION = "lorem ipsum de description foes here"
var notificationId = 1

private const val TAG = "NotificationBroadcast"

class NotificationBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG, "************************************")
        Log.d(TAG, "onReceive Called")

        val nm = NotificationsSharedPref(context!!)


        /**
         * When the device restarts all the notifictions are lost. This method adds all the notifications
         * back when it detects that the device has powered back on. It retreives the information about
         * the notifications from [NotificationsSharedPref] and recreates them all with the appropriate
         * alarm manager
         */
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            Log.d(TAG, "onBootReceived")
            /*val mServiceIntent = Intent(context, BootService::class.java)
            context?.startService(mServiceIntent)*/

            Log.d(TAG, "the keys in sharedPreferences are--(flyover, alarm)--------")
            Log.d(TAG, "size is ${nm.getSharedPrefValues().size}")
            nm.printAll()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            /*val notificationValues = nm.getSharedPrefValues()
            notificationValues.forEach {
                val myIntent = Intent(context, NotificationBroadcast::class.java)

                //add all of the extra information to the notification
                //Format is: timeStampOfAlarm, lat, long, timeString
                val splitInfoString = (it as String).split(",")
                val timeOfAlarm = splitInfoString[0].toLong()
                val lat = splitInfoString[1].toDouble()
                val long = splitInfoString[2].toDouble()
                val timeString = splitInfoString[3]

                intent.putExtra(INTENT_TIME_REQUEST_CODE, timeForKey) //key
                intent.putExtra(INTENT_LAT_LNG_STRING, "$lat, $long")
                intent.putExtra(INTENT_TIME_STRING, "$timeString")
                intent.putExtra(INTENT_FLYOVER_TIME, flyoverTime) //flyover time


                Log.d(TAG, "in shared pref $it")
                val pendingIntent = PendingIntent.getBroadcast(context, it.hashCode(), myIntent, 0)
                alarmManager.set(AlarmManager.RTC_WAKEUP, it as Long, pendingIntent)
            }*/

            //go through all the elements in sharedPreferences and add alarms for all the notifications
            val notificationKeys = nm.getSharedPrefKeys()
            notificationKeys.forEach {
                val myIntent = Intent(context, NotificationBroadcast::class.java)

                //add all of the extra information to the notification
                //Format is: timeStampOfAlarm, lat, long, timeString
                val splitInfoString = (nm.get(it) as String).split(",")
                val timeOfAlarm = splitInfoString[0].toLong()
                val lat = splitInfoString[1].toDouble()
                val long = splitInfoString[2].toDouble()
                val timeString = splitInfoString[3]
                val searchString = splitInfoString[4]

                Log.d(TAG, "for key $it: value is ${nm.get(it)}")

                Log.d(TAG, "putting $it into INTENT_TIME_REQUEST_CODE")
                myIntent.putExtra(INTENT_TIME_REQUEST_CODE, it) //key
                myIntent.putExtra(INTENT_LAT_LNG_STRING, "$lat, $long")
                myIntent.putExtra(INTENT_TIME_STRING, timeString)
                myIntent.putExtra(INTENT_FLYOVER_TIME, it.toLong()) //flyover time
                myIntent.putExtra(INTENT_SEARCH_STRING, searchString)

                val pendingIntent = PendingIntent.getBroadcast(context, it.hashCode(), myIntent, 0)
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeOfAlarm, pendingIntent)
            }

            Log.d(TAG, "end on BOOT_COMPLETED")
            return
        }


        Log.d(TAG, "did not enter boot completed")

        val requestCodeToDelete = intent?.getLongExtra(INTENT_TIME_REQUEST_CODE, -1)
        Log.d(TAG, "RequestCode To delete is $requestCodeToDelete")



        val latLngString = intent?.getStringExtra(INTENT_LAT_LNG_STRING)
        val splitLatLngString = latLngString?.split(",")

        val latParam = splitLatLngString?.get(0)?.toDouble()
        val longParam = splitLatLngString?.get(1)?.toDouble()

        val timeString = intent?.getStringExtra(INTENT_TIME_STRING)
        val time = intent?.getLongExtra(INTENT_FLYOVER_TIME, -1L)
        val searchString = intent?.getStringExtra(INTENT_SEARCH_STRING)

        if (context != null) {
            createNotification(context, latParam, longParam, timeString, time!!, searchString)
        }

        Log.d(TAG, "onReceive intent request code is ${requestCodeToDelete}.}")

        //delete the notification with the request code passed in the intent
        nm.delete(requestCodeToDelete!!)

        Log.d(TAG, "After deleting $requestCodeToDelete; size is ${nm.getSharedPrefValues().size}")
    }

    companion object {
        fun createNotification(context: Context, lat: Double?, long: Double?, timeString: String?, time: Long, searchString: String?) {

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

            Log.d(TAG, "createNotification. time is $time")
            // Create an explicit intent for an Activity in your app
            val intent = Intent(context, MainActivity::class.java).apply {
                if (lat != null && long != null && time != -1L) {
                    putExtra(NOTIFICATION_LAT, lat)
                    putExtra(NOTIFICATION_LONG, long)
                    putExtra(NOTIFICATION_TIME, time)
                    putExtra(NOTIFICATION_LAUNCHED_MAIN_ACTIVITY, true)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val infoMessage = if (timeString == null) {
                context.getString(R.string.flyoverNotification, "your area", "UNKNOWN")
            } else {
                context.getString(R.string.flyoverNotification, searchString, timeString)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(context.getString(R.string.icesatFlyover))
                .setContentText(infoMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(infoMessage))
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

