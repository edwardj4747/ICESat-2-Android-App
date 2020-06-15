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
//Todo:test this for dates way far away in the future
private const val DATE_DIVISOR = 1000

class DownloadData {

    private val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time

    private val comparator = object : Comparator<Point> {
        override fun compare(o1: Point?, o2: Point?): Int {
            if (o1 != null && o2 != null) {
                Log.d(TAG, "o1 is $o1; o2 is $o2. Comparator returns ${((o1.dateObject.time - o2.dateObject.time) / DATE_DIVISOR).toInt()}")
                return ((o1.dateObject.time - o2.dateObject.time) / DATE_DIVISOR).toInt()
                //return ((o1.dateObject.time - twoToTheTenth) - (o2.dateObject.time - twoToTheTenth)).toInt()
            } else {
                Log.d(TAG, "Error in comparator method")
                throw IllegalArgumentException("Passed a null date into the comparator")
            }
        }
    }


    //return true if any points meet search criteria. False if no points meet criteria
    suspend fun startDownload(string: String) : Boolean{
        Log.d(TAG, "startDownload method begins")
        var resultsFound = false
        val job = CoroutineScope(Dispatchers.IO).launch {
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
                        val lon = individualPoint.getDouble("lon")
                        val lat = individualPoint.getDouble("lat")

                        //convert date + time to users timezone. Returns an array with format {stringRepresentation of Date, dateObject}
                        val convertedDateTime: Deferred<Array<Any?>> = async(Dispatchers.IO) {
                            convertDateTime(date, time)
                        }
                        //Wait until conversion is completed. Add the point only if it is in future
                        if (convertedDateTime.await()[0] != DATE_ALREADY_PASSED) {
                            val newPoint = Point(date, time, lon, lat, convertedDateTime.await()[0] as String, convertedDateTime.await()[1] as Date)
                            pointsArrayList.add(newPoint)
                        }
                    }
                    val mainActivityViewModel = MainActivity.getMainViewModel()

                    //if there are any results from the search. Sort them and split them accordingly
                    if (pointsArrayList.size > 0) {
                        Log.d(TAG, "prior to sorting $pointsArrayList")
                        //sort the pointsArrayList based on date with earlier dates coming at the beginning
                        val sortPointArrayUnit: Deferred<Unit> = async {
                            Collections.sort(pointsArrayList, comparator)
                        }
                        sortPointArrayUnit.await()

                        Log.d(TAG, "AFter sorting $pointsArrayList")

                       /* val allPointChains: Deferred<ArrayList<ArrayList<Point>>> = async {
                            splitPointsByDate(pointsArrayList)
                        }*/

                        if (mainActivityViewModel != null) {
                            //TODO: remove allPointsList in ViewModel?
                            Log.d(TAG, "Posting all points list to viewmodel")
                            mainActivityViewModel.allPointsList.postValue(pointsArrayList)
                            //mainActivityViewModel.allPointsChain.postValue(allPointChains.await())
                            resultsFound = true
                        }
                    } else {
                        //if we don't find any results post an empty list. Removes carryovers from displaying in searches that have no result
                        mainActivityViewModel?.allPointsChain?.postValue(ArrayList<ArrayList<Point>>())
                        resultsFound = false
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

        job.join()
        Log.d(TAG, "startDownload method ends. returning $resultsFound")
        return resultsFound
    }

    private fun convertDateTime(dateString: String, timeString: String): Array<Any?> {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val inputDate = "$dateString $timeString"
        val dateTimeToConvert = inputFormat.parse(inputDate)
        if (dateTimeToConvert.before(currentTime)) {
            return arrayOf(DATE_ALREADY_PASSED, null)
        }
        val outputFormat = SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss aaa", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        val convertedDateString = outputFormat.format(dateTimeToConvert)
        return arrayOf(convertedDateString, dateTimeToConvert)
    }

    private fun splitPointsByDate(allPointsList: ArrayList<Point>): ArrayList<ArrayList<Point>> {
        val timingThreshold = 60 //TODO: check with tom that this is a good threshold
        var chainIndex = 0
        val splitByDateArrayList = ArrayList<ArrayList<Point>>()
        splitByDateArrayList.add(ArrayList<Point>())
        splitByDateArrayList[0].add(allPointsList[0])
        var startingDateTime = allPointsList[0].dateObject.time
        for (i in 1 until allPointsList.size) {
            if (startingDateTime + timingThreshold * 1000 > allPointsList[i].dateObject.time) {
                splitByDateArrayList[chainIndex].add(allPointsList[i])
            } else {
                startingDateTime = allPointsList[i].dateObject.time
                splitByDateArrayList.add(ArrayList())
                chainIndex++
                splitByDateArrayList[chainIndex].add(allPointsList[i])
            }
        }

        return splitByDateArrayList
    }
}