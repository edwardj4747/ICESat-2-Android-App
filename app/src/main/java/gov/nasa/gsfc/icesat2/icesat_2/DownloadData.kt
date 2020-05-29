package gov.nasa.gsfc.icesat2.icesat_2

import android.util.Log
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.SearchFragment
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

private const val TAG = "DownloadData"

class DownloadData {
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
                        val lon = individualPoint.getDouble("lon")
                        val lat = individualPoint.getDouble("lat")
                        val newPoint = Point(date, time, lon, lat)
                        pointsArrayList.add(newPoint)
                    }
                    val searchViewModel = SearchFragment.getSearchViewModel()
                    if (searchViewModel != null) {
                        searchViewModel.allPointsList.postValue(pointsArrayList)
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
}