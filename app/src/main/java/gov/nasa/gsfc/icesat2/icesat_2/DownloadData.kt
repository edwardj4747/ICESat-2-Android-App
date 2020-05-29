package gov.nasa.gsfc.icesat2.icesat_2

import android.util.Log
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(string)
                val jsonText = url.readText()
                val jsonObject = JSONObject(jsonText)
                Log.d(TAG, jsonObject.getString("status"))
            } catch (e: MalformedURLException) {
                Log.d(TAG, "Malformed URL Exception ${e.message}")
            } catch (e: IOException) {
                Log.d(TAG, "IO Exception ${e.message}")
            } catch (e: Exception) {
                Log.d(TAG, "Exception ${e.message}")
            }
        }
    }
}