package gov.nasa.gsfc.icesat2.icesat_2

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import kotlinx.android.synthetic.main.activity_main_nav.*
import kotlinx.coroutines.*


private const val TAG = "MainActivity"
private const val LOCATION_REQUEST_CODE = 6

class MainActivity : AppCompatActivity(), ISearchFragmentCallback {

    private lateinit var navController: NavController
    private var currentlySearching = false //to make sure only one search is running at a time

    companion object {
        private lateinit var mainViewModel: MainViewModel

        fun getMainViewModel(): MainViewModel? {
            if (this::mainViewModel.isInitialized) {
                return mainViewModel
            }
            return null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_nav)

        //setSupportActionBar(toolbar)
        //val appBarConfiguration = AppBarConfiguration(navController.graph)
        //toolbar.setupWithNavController(navController, appBarConfiguration)

        navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_search, R.id.navigation_favorites, R.id.navigation_info))

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottom_nav_view.setupWithNavController(navController)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)


    }

    override fun searchButtonPressed(lat: Double, long: Double, radius: Double, calledFromSelectOnMap: Boolean) {

        val serverLocation = "http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=$lat&lon=$long&r=$radius&u=miles"
        Log.d(TAG, "MainActivity: starting download from $serverLocation")

        Log.d(TAG, "isNetworkConnected ${isNetworkConnected()}")

        var searchResultsFound = false

        /**
         * If there is not a current search happening and user is connected to a network THEN
         *
         * Determine if there are any results and store that in the searchFoundResults variable (true/ false)
         * Wait until that completes (jobDownloadData.join()) and if results found -> show them the results on Map
         * otherwise display a dialog that no results were found
         */
        if (!currentlySearching) {
            if (isNetworkConnected()) {
                currentlySearching = true
                CoroutineScope(Dispatchers.IO).launch {
                    val jobDownloadData = CoroutineScope(Dispatchers.IO).launch {
                        val downloadData = DownloadData()
                        val result: Deferred<Boolean> = async {
                            downloadData.startDownload(serverLocation)
                        }
                        searchResultsFound = result.await()
                    }

                    jobDownloadData.join()

                    Log.d(TAG, "searchResultsFound = $searchResultsFound")
                    if (searchResultsFound) {
                        Log.d(TAG, "YAY!! Search results found")
                        if (calledFromSelectOnMap) {
                            launchMapOnMainThread(lat, long, radius, R.id.action_selectOnMapFragment_to_resultsHolderFragment)
                        } else {
                            launchMapOnMainThread(lat, long, radius, R.id.action_navigation_search_to_resultsHolderFragment)
                        }
                    } else {
                        Log.d(TAG, "No Search results found")
                        showDialogOnMainThread(R.string.noResults, R.string.noResultsDetails, R.string.backToSearch)
                    }
                    currentlySearching = false
                }
            } else {
                showDialog(R.string.noNetworkTitle, R.string.noNetworkDescription, R.string.ok)
            }
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return cm.activeNetwork != null
        }
        return cm.activeNetworkInfo != null
    }

    override fun useCurrentLocationButtonPressed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissionDialog()
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates("gps", 10000, 50F, object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                if (location != null) {
                    Log.d(TAG, "lat is ${location.latitude}, long is ${location.longitude}")
                } else {
                    Log.d(TAG, "onLocationCHanged but location is null")
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String?) {
                Log.d(TAG, "onProvider enabled with $provider")
            }

            override fun onProviderDisabled(provider: String?) {
                Log.d(TAG, "provider is disabled $provider")
                showDialogOnMainThread(R.string.locationOffTitle, R.string.locationOffDescription, R.string.ok)
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermission Callback")
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION_REQUEST_CODE) {
            //clicked accept
            Log.d(TAG, "permission accepted")
        } else {
            //clicked deny
            Log.d(TAG, "Permission Denied in Callback")
            grantAccessSnackbar()
        }
    }

    private fun requestLocationPermissionDialog() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_REQUEST_CODE
        )
    }

    private fun grantAccessSnackbar() {
        Snackbar.make(findViewById(R.id.constraintLayout), R.string.locationSnackbar, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.grantAccess) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d(TAG, "should show request true")
                    requestLocationPermissionDialog()
                } else {
                    Log.d(TAG, "should show request false")
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", this.packageName, null)
                    startActivity(intent)
                }
            }
            .show()
    }

    override fun selectOnMapButtonPressed() {
        navController.navigate(R.id.selectOnMapFragment)
    }

    private fun launchMapOnMainThread(lat: Double, long: Double, radius: Double, navigationActionID: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            showMap(navigationActionID)
            mainViewModel.searchCenter.value = LatLng(lat, long)
            mainViewModel.searchRadius.value = radius
        }
    }

    private fun showMap(navigationActionID: Int) {
        //navController.navigate(R.id.action_navigation_home_to_mapFragment2)
        navController.navigate(navigationActionID)
    }


    private fun showDialogOnMainThread(title: Int, message: Int, buttonMessage: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            showDialog(title, message, buttonMessage)
        }
    }

    private fun showDialog(title: Int, message: Int, buttonMessage: Int) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setMessage(message)
            ?.setTitle(title)
            ?.setPositiveButton(buttonMessage) { dialog, which ->
                Log.d(TAG, "Dialog positive button clicked")
            }
        alertBuilder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //TODO: This feels like a very bad solution
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }



}
