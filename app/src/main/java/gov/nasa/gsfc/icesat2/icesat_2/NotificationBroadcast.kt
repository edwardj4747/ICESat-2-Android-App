package gov.nasa.gsfc.icesat2.icesat_2

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

private const val CHANNEL_ID = "NotificationsTest"
private const val DESCRIPTION = "lorem ipsum de description foes here"
var notificationId = 1

private const val TAG = "NotificationBroadcast"

class NotificationBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive Called")
        if (context != null) {
            createNotification(context)
        }
    }

    companion object {
        fun createNotification(context: Context) {

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
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(context.getString(R.string.icesatFlyover))
                .setContentText(context.getString(R.string.flyoverNotification))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        }
    }

}