package gov.nasa.gsfc.icesat2.icesat_2

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

private const val TAG = "DownloadData"
private const val DATE_ALREADY_PASSED = "DATE_ALREADY_PASSED"

class DownloadData {

    private val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time

    val comparator = object : Comparator<Point> {
        override fun compare(o1: Point?, o2: Point?): Int {
            if (o1 != null && o2 != null) {
                return (o1.dateObject.time - o2.dateObject.time).toInt()
            } else {
                Log.d(TAG, "Error in comparator method")
                throw IllegalArgumentException("Passed a null date into the comparator")
            }
        }

    }


    companion object {
        val testArray = ArrayList<TestPoint>()

        private val testComparator = object : Comparator<TestPoint> {
            override fun compare(o1: TestPoint?, o2: TestPoint?): Int {
                if (o1 != null && o2 != null) {
                    return (o1.dateObject.time - o2.dateObject.time).toInt()
                } else {
                    Log.d(TAG, "Error in comparator method")
                    throw IllegalArgumentException("Passed a null date into the comparator")
                }
            }
        }


        fun getTestArrayMethod(): ArrayList<TestPoint> {
            return testArray
        }

        fun getSortedTestArrayMethod(): ArrayList<TestPoint> {
            Collections.sort(testArray, testComparator)
            return testArray
        }
    }



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

                        //convert date/ time to users timezone. Returns an array with format {stringRepresentation of Date, dateObject}
                        val convertedDateTime: Deferred<Array<Any?>> = async(Dispatchers.IO) {
                            convertDateTime(date, time)
                        }

                        val lon = individualPoint.getDouble("lon")
                        val lat = individualPoint.getDouble("lat")

                        //Wait until conversion is completed. Add the point only if it is in future
                        if (convertedDateTime.await()[0] != DATE_ALREADY_PASSED) {
                            val newPoint = Point(date, time, lon, lat, convertedDateTime.await()[0] as String, convertedDateTime.await()[1] as Date)
                            pointsArrayList.add(newPoint)
                        }
                    }

                    Log.d(TAG, "prior to sorting pointArrayList is $pointsArrayList")
                    val sortPointArrayUnit: Deferred<Unit> = async {
                        Collections.sort(pointsArrayList, comparator)
                    }
                    sortPointArrayUnit.await()
                    Log.d(TAG, "after sorting pointArrayList is $pointsArrayList")

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

    private fun convertDateTime(dateString: String, timeString: String): Array<Any?> {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val inputDate = "$dateString $timeString"
        val dateTimeToConvert = inputFormat.parse(inputDate)
        if (dateTimeToConvert.before(currentTime)) {
            //I think this means that the date has already passed
            Log.d(TAG, "inputDate has already passed $inputDate")
            return arrayOf(DATE_ALREADY_PASSED, null)
        }
        val outputFormat = SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss aaa", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        val convertedDateString = outputFormat.format(dateTimeToConvert)
        testArray.add(TestPoint(convertedDateString, dateTimeToConvert))
        return arrayOf(convertedDateString, dateTimeToConvert)
    }
}