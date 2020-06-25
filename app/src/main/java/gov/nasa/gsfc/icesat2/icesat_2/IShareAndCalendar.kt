package gov.nasa.gsfc.icesat2.icesat_2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.maps.GoogleMap
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.max

private const val TAG = "IShare"

interface IShareAndCalendar : IGeocoding {

    fun addToCalendar(context: Context, title: String, startTime: Long, lat: Double, long: Double) {
        val cityLocation = getAddress(context, lat, long) //method in IGeocoding

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + 60 * 1000) //end time is one minute later
            putExtra(CalendarContract.Events.EVENT_LOCATION, cityLocation)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun showShareScreen(mMap: GoogleMap, activity: Activity, context: Context, flyoverDates: ArrayList<String>) {
        //take screenshot + show share screen dialog

        val screenshot: Bitmap? = null
        mMap.snapshot { mapBitmap ->
            if (mapBitmap != null) {

                val combinedBitmap = addLogoToBitmap(context, mapBitmap)

                val file = File(activity.externalCacheDir, "ICESat-2Flyover.png")
                val fileOutputStream = FileOutputStream(file)
                //mapBitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream)
                combinedBitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

                val fileProviderUri = FileProvider.getUriForFile(context, context.applicationContext.packageName
                        + ".provider", file);
                //install.setDataAndType(apkURI, mimeType);
                //install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                var searchString = MainActivity.getMainViewModel()?.searchString?.value
                if (searchString == "Your Location") {
                    searchString = "our area"
                } else if (searchString == null) {
                    searchString = ""
                }

                var datesOfOccurrence = flyoverDates.toString().substring(1, flyoverDates.toString().length - 1)
                if (flyoverDates.size > 1) {
                    datesOfOccurrence = datesOfOccurrence.replaceAfterLast(",", context.getString(R.string.icesatShareLastDate, flyoverDates[flyoverDates.size - 1]))
                }
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.icesatFlyover))
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.icesatShare, searchString, datesOfOccurrence))
                    putExtra(Intent.EXTRA_STREAM, fileProviderUri)
                    type = "image/png"
                }

                Log.d(TAG, "search string is ${MainActivity.getMainViewModel()?.searchString?.value}")
                val shareIntent = Intent.createChooser(sendIntent, null)

                if (shareIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(shareIntent)
                } else {
                    Toast.makeText(context, "Error Occurred Please Try Again", Toast.LENGTH_SHORT).show()
                }
            }
            Log.d(TAG, "snapshot is ready method called. Map screenshot is $screenshot")
        }
    }

    private fun addLogoToBitmap(context: Context, mapBitmap: Bitmap) : Bitmap {
        //determine height of the screen in pixels
        val displayMetrics = context.resources.displayMetrics
        val displayHeight = displayMetrics.heightPixels * 0.2F
        val paddingValue = displayHeight * 0.1F

        //val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.white_logo)
        //val logoBitmap = scaleLogoBitmap(context, mapBitmap.width, paddingValue.toInt())
        val logoBitmap = scaleLogoBitmap(context, displayHeight.toInt(), paddingValue.toInt())
        val resultBitmap = Bitmap.createBitmap(max(logoBitmap.width, mapBitmap.width), logoBitmap.height + mapBitmap.height, mapBitmap.config)

        val canvas = Canvas(resultBitmap)
        canvas.drawColor(ContextCompat.getColor(context, R.color.backgroundColor))
        val leftOffset = (canvas.width - logoBitmap.width) / 2F
        canvas.drawBitmap(logoBitmap, leftOffset, paddingValue, null)
        canvas.drawBitmap(mapBitmap, 0F, logoBitmap.height.toFloat() + paddingValue * 1.5F, null)
        logoBitmap.recycle()
        logoBitmap.recycle()

        return resultBitmap
    }

    private fun scaleLogoBitmap(context: Context, maxSize: Int, paddingValue: Int) : Bitmap {
        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.white_logo)
        var width: Int = logoBitmap.width
        var height: Int = logoBitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        height = maxSize
        width = (height * bitmapRatio).toInt()
        return Bitmap.createScaledBitmap(logoBitmap, width - 2 * paddingValue, height - 2 * paddingValue, true)
    }
}