package gov.nasa.gsfc.icesat2.icesat_2

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "DownloadData"
private const val DATE_ALREADY_PASSED = "DATE_ALREADY_PASSED"

class DownloadData {

    private val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time


    fun startDownload(string: String) {
        Log.d(TAG, "startDownload method begins")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(string)
                val jsonText = url.readText()
                val jsonObject = JSONObject(jsonText)

                val state = jsonObject.getString("state")
                if (state == "true") {
                    val pointsArrayList = ArrayList<Point>()
                    val queryResult = jsonObject.getJSONArray("result")
                    for (i in 0 until queryResult.length()) {
                        val individualPoint = queryResult.getJSONObject(i)
                        val date = individualPoint.getString("date")
                        val time = individualPoint.getString("time")

                        //calculate conversion without blocking thread
                        val convertedDateTime: Deferred<String> = async(Dispatchers.IO) {
                            convertDateTime(date, time)
                        }
                        val lon = individualPoint.getDouble("lon")
                        val lat = individualPoint.getDouble("lat")
                        //once conversion is completed, add the point if it is in the future
                        if (convertedDateTime.await() != DATE_ALREADY_PASSED) {
                            val newPoint = Point(date, time, lon, lat, convertedDateTime.await())
                            pointsArrayList.add(newPoint)
                        }
                    }
                    val mainActivityViewModel = MainActivity.getMainViewModel()
                    if (mainActivityViewModel != null) {
                        mainActivityViewModel.allPointsList.postValue(pointsArrayList)
                    }
                } else {
                    Log.d(TAG, "state is not true $state")
                    //TODO: Handle these cases
                }


            } catch (e: MalformedURLException) {
                Log.d(TAG, "Malformed URL Exception ${e.message}")
            } catch (e: IOException) {
                Log.d(TAG, "IO Exception ${e.message}")
            } catch (e: Exception) {
                Log.d(TAG, "Exception ${e.message}")
            }
        }
        Log.d(TAG, "startDownload method ends")
    }

    private fun convertDateTime(dateString: String, timeString: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val inputDate = "$dateString $timeString"
        val dateTimeToConvert = inputFormat.parse(inputDate)
        if (dateTimeToConvert.before(currentTime)) {
            //I think this means that the date has already passed
            Log.d(TAG, "inputDate has already passed $inputDate")
            return DATE_ALREADY_PASSED
        }
        val outputFormat = SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss aaa", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        return outputFormat.format(dateTimeToConvert)
    }
}