package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "DownloadData"
private const val DATE_ALREADY_PASSED = "DATE_ALREADY_PASSED"
private const val DATE_IN_FUTURE = "DATE_IN_FUTURE"
//Todo:test this for dates way far away in the future
private const val DATE_DIVISOR = 1000
private const val JOB_TIMEOUT = 4000L

class DownloadData(private val url: URL, context: Context) {

    private lateinit var mainSearchJob: Job
    private var listener: IDownloadDataErrorCallback = context as MainActivity

    private val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time

    private val comparator = object : Comparator<Point> {
        override fun compare(o1: Point?, o2: Point?): Int {
            if (o1 != null && o2 != null) {
                return ((o1.dateObject.time - o2.dateObject.time) / DATE_DIVISOR).toInt()
                //return ((o1.dateObject.time - twoToTheTenth) - (o2.dateObject.time - twoToTheTenth)).toInt()
            } else {
                Log.d(TAG, "Error in comparator method")
                throw IllegalArgumentException("Passed a null date into the comparator")
            }
        }
    }

    suspend fun startDownloadDataProcess() : Boolean{
        var result = false
        withContext(Dispatchers.IO) {
            val job = withTimeoutOrNull(JOB_TIMEOUT) {
                result = startDownload() // wait until job is done
                Log.d(TAG, "download finished")
            }

            if(job == null){
                Log.d(TAG, "Canceling search job")
                mainSearchJob.cancel()

                listener.addErrorToSet(SearchError.TIMED_OUT)
            }

        }
        Log.d(TAG, "start download process result is $result")
        return result
    }

    //return true if any points meet search criteria. False if no points meet criteria
    suspend fun startDownload(): Boolean{
        Log.d(TAG, "startDownload method begins")
        var resultsFound = false
        mainSearchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonText = url.readText()
                val jsonObject = JSONObject(jsonText)

                val state = jsonObject.getString("state")
                if (state == "true") {
                    val pointsArrayList = ArrayList<Point>()
                    val pastPointsArrayList = ArrayList<Point>()
                    val queryResult = jsonObject.getJSONArray("result")
                    for (i in 0 until queryResult.length()) {
                        val individualPoint = queryResult.getJSONObject(i)
                        val date = individualPoint.getString("date")
                        val time = individualPoint.getString("time")
                        val lon = individualPoint.getDouble("lon")
                        val lat = individualPoint.getDouble("lat")


                        //TODO: refactor according to new properties specified in convertDateTimeMethod
                        //convert date + time to users timezone. Returns an array with format {stringRepresentation of Date, dateObject}
                        val convertedDateTime: Deferred<Array<Any?>> = async(Dispatchers.IO) {
                            convertDateTime(date, time)
                        }

                        //Wait until conversion is completed. Add the point only if it is in future
                        if (convertedDateTime.await()[8] != DATE_ALREADY_PASSED) {
                            val newPoint = Point(convertedDateTime.await()[0] as String, convertedDateTime.await()[1] as String,
                                convertedDateTime.await()[2] as String, convertedDateTime.await()[3] as String, convertedDateTime.await()[4] as String,
                            convertedDateTime.await()[5] as String, convertedDateTime.await()[6] as String, lon, lat, convertedDateTime.await()[7] as Date)
                            pointsArrayList.add(newPoint)
                        } else {
                            val oldPoint = Point(convertedDateTime.await()[0] as String, convertedDateTime.await()[1] as String,
                                convertedDateTime.await()[2] as String, convertedDateTime.await()[3] as String, convertedDateTime.await()[4] as String,
                                convertedDateTime.await()[5] as String, convertedDateTime.await()[6] as String, lon, lat, convertedDateTime.await()[7] as Date)
                            pastPointsArrayList.add(oldPoint)
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

                        //sort the past points
                        val sortPastPointArrayUnit: Deferred<Unit> = async {
                            Collections.sort(pastPointsArrayList, comparator)
                        }
                        sortPastPointArrayUnit.await()

                       /* val allPointChains: Deferred<ArrayList<ArrayList<Point>>> = async {
                            splitPointsByDate(pointsArrayList)
                        }*/

                        if (mainActivityViewModel != null) {
                            //TODO: remove allPointsList in ViewModel?
                            Log.d(TAG, "Posting all points list to viewmodel")
                            mainActivityViewModel.allPointsList.postValue(pointsArrayList)
                            //mainActivityViewModel.allPointsChain.postValue(allPointChains.await())
                            resultsFound = true

                            //post the oldPoints too
                            mainActivityViewModel.pastPointsList.postValue(pastPointsArrayList)
                        }
                    } else {
                        //if we don't find any results post an empty list. Removes carryovers from displaying in searches that have no result
                        mainActivityViewModel?.allPointsChain?.postValue(ArrayList<ArrayList<Point>>())

                        // todo: no results
                        listener.addErrorToSet(SearchError.NO_RESULTS)
                        resultsFound = false
                    }
                } else {
                    Log.d(TAG, "state is not true $state")
                    //TODO: Handle these cases
                }

            /*} catch (e: IOException) {
                Log.d(TAG, "IO Exception ${e.message}")*/
            } catch (e: Exception) {
                Log.d(TAG, "Exception ${e.message}")
                //listener.searchTimedOut()
            }
        }

        mainSearchJob.join()
        Log.d(TAG, "startDownload method ends. returning $resultsFound")
        return resultsFound
    }

    /**
     * Converts the downloaded time/data from UTC to users time zone.
     * @param dateString downloaded date of form 22-7-2020 (ie: July 22, 2020)
     * @param timeString time of the satellite flyover in UTC
     * If the date has already passed, will return arrayOf(DATE_ALREADY_PASSED, null) otherwise
     * @return arrayOf(convertedDateString, dayOfWeek, date, year, timeString, AM/PM, timezone, dateTimeToConvert, inPast/inFuture)
     */
    private fun convertDateTime(dateString: String, timeString: String): Array<Any?> {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val inputDate = "$dateString $timeString"
        val dateTimeToConvert = inputFormat.parse(inputDate)
        /*if (dateTimeToConvert.before(currentTime)) {
            return arrayOf(DATE_ALREADY_PASSED, null)
        }*/
        val outputFormat = SimpleDateFormat("EEE, MMM d, yyyy, hh:mm:ss, aaa, z", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        val convertedDateString = outputFormat.format(dateTimeToConvert)

        //split up the date string
        val convertedDateStringSplit = convertedDateString.split(",")

        val alreadyPassed = if (dateTimeToConvert.before(currentTime)) {
            DATE_ALREADY_PASSED
        } else {
            DATE_IN_FUTURE
        }

        return arrayOf(convertedDateString, convertedDateStringSplit[0], convertedDateStringSplit[1].trimStart(),
            convertedDateStringSplit[2].trimStart(), convertedDateStringSplit[3].trimStart(), convertedDateStringSplit[4].trimStart(),
            convertedDateStringSplit[5].trimStart(), dateTimeToConvert, alreadyPassed)

        //return arrayOf(convertedDateString, dateTimeToConvert)
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


    fun downloadTrackingData(url: URL) : ArrayList<TrackingPoint> {
        Log.d(TAG, "downloadTracking data Starts")
        val trackingData = ArrayList<TrackingPoint>()
        try {
            val data = url.readText()
            Log.d(TAG, "Data is $data")

            val jsonArray = JSONArray(data)
            Log.d(TAG, "size is ${jsonArray.length()}")
            for (i in 0 until jsonArray.length()) {
                val currentObj = jsonArray.getJSONObject(i)
                val timeInMillis = currentObj.getLong("timeInMillis")
                val lat = currentObj.getDouble("lat")
                val lon = currentObj.getDouble("lon")
                val description = currentObj.getString("dateString")
                trackingData.add(TrackingPoint(timeInMillis, lat, lon, description))
            }
            Log.d(TAG, "trackingData \n $trackingData")
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "Downloading Tracking data exception ${e.message}")
        }
        return trackingData
    }
}

data class TrackingPoint(val timeInMillis:Long, val lat: Double, val long: Double, val description: String) {}