package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.SearchFragment
import kotlinx.android.synthetic.main.fragment_search.*


private const val TAG = "MainActivity"
private const val LAT_INPUT_ERROR = "Please Enter Latitude between -86.0 and 86.0 ${0x00B0.toChar()}N"
private const val LONG_INPUT_ERROR = "Please Enter Longitude between -180.0 and 180.0 ${0x00B0.toChar()}E"
private const val RADIUS_INPUT_ERROR = "Please Enter Radius between 1.1 and 25.0"

class MainActivity : AppCompatActivity(), ISearchFragmentCallback {

    private val fragmentManger = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentTransaction = fragmentManger.beginTransaction()
        val searchFragment = SearchFragment()
        searchFragment.addSearchFragmentCallbackListener(this) // important!!
        fragmentTransaction.apply {
            replace(R.id.fragmentContainer, searchFragment)
            commit()
        }




        /*val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)*/
    }

    override fun searchButtonPressed() {
        Log.d(TAG, "MainActivity: Search Button Pressed")

        val inputs = allInputsValid() //returns array of {lat, long, radius} if valid. null if not valid
        if (inputs != null) {
            val unit = if (unitSpinner.selectedItem.toString() == "Kilometers") {
                "kilometers"
            } else {
                "miles"
            }
            //http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=-38.9&lon=78.1&r=25&u=miles
            val serverLocation = "http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=${inputs[0]}&lon=${inputs[1]}&r=${inputs[2]}&u=$unit"
            Log.d(TAG, "starting download from $serverLocation")
            val downloadData = DownloadData()
            downloadData.startDownload(serverLocation)
        }

        Log.d(TAG, "MainActivity: SearchButtonPressed ends")
    }

    private fun createSnackBar(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Snackbar.make(findViewById(R.id.container), text, Snackbar.LENGTH_LONG)
                .setAction("OK") {  }
                .setBackgroundTint(resources.getColor(R.color.colorPrimary, null))
                .show()
        } else {
            Snackbar.make(findViewById(R.id.container), text, Snackbar.LENGTH_LONG)
                .setAction("OK") {  }
                .setBackgroundTint(resources.getColor(R.color.colorPrimary))
                .show()
        }
    }

    //return null if there is an error with one of the inputs. Otherwise return array of (lat, lng, radius
    private fun allInputsValid(): DoubleArray? {
        //lat range -86, 86; lon range -180 180
        if (editTextLat.text.toString() == "") {
            createSnackBar(LAT_INPUT_ERROR)
            return null
        }
        val lat = editTextLat.text.toString().toDouble()
        if (lat < - 86 || lat > 86) {
            createSnackBar(LAT_INPUT_ERROR)
            return null
        }

        if (editTextLon.text.toString() == "") {
            createSnackBar(LONG_INPUT_ERROR)
            return null
        }
        val long = editTextLon.text.toString().toDouble()
        if (long < -180 || long > 180) {
            createSnackBar(LONG_INPUT_ERROR)
            return null
        }

        if (editTextRadius.text.toString() == "") {
            createSnackBar(RADIUS_INPUT_ERROR)
            return null
        }
        val radius = editTextRadius.text.toString().toDouble()
        if (radius < 1.1 || radius > 25) {
            createSnackBar(RADIUS_INPUT_ERROR)
            return null
        }
        return doubleArrayOf(lat, long, radius)
    }
}
