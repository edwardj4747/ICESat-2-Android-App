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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.SearchFragment
import kotlinx.android.synthetic.main.activity_main_nav.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.*
import java.net.URL

const val DEFAULT_SEARCH_RADIUS = 25.0
private const val TAG = "MainActivity"
private const val LOCATION_REQUEST_CODE = 6

class MainActivity : AppCompatActivity(), ISearchFragmentCallback, IDownloadDataErrorCallback {

    private lateinit var navController: NavController
    private var currentlySearching = false //to make sure only one search is running at a time
    private var navHostFragment: Fragment? = null
    private var gpsEnabled = false
    private lateinit var locationManager: LocationManager
    private var simpleSearch = true
    private var currentDestination: NavDestination? = null
    private var previousDestination: NavDestination? = null
    private var searchFragmentDestination: NavDestination? = null

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
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_search, R.id.navigation_favorites, R.id.navigation_gallery, R.id.navigation_info))

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottom_nav_view.setupWithNavController(navController)

        currentDestination = navController.currentDestination

        navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController, destination: NavDestination, arguments: Bundle?) {
                //maintain track of where we were in the search
                previousDestination = currentDestination
                currentDestination = destination

               if (destination.label == "Home" && searchFragmentDestination?.label == "Search Results" && previousDestination?.label != "Search Results" && previousDestination?.label != "Select Location On Map") {
                    Log.d(TAG, "at search and searchFrag destination is Search Results")
                   launchMapNoAnimation()
                } else if (destination.label == "Home" || destination.label == "Search Results") {
                    Log.d(TAG, "searchFrag: setting searchFrag destination")
                    searchFragmentDestination = destination
                }

                Log.d(TAG, "searchFrag destination is ${searchFragmentDestination?.label} \n previous destination is ${previousDestination?.label}")
            }
        })

        navHostFragment= supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun searchButtonPressed(lat: Double, long: Double, radius: Double, calledFromSelectOnMap: Boolean) {



        val serverLocation = "http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=$lat&lon=$long&r=$radius&u=miles"
        Log.d(TAG, "MainActivity: starting download from $serverLocation")

        Log.d(TAG, "isNetworkConnected ${isNetworkConnected()}")

        var searchResultsFound = false


        val u = URL(serverLocation)
        val dd = DownloadData(u, this)
        CoroutineScope(Dispatchers.IO).launch {
            dd.startDownloadDataProcess()
        }
        return


        /**
         * If there is not a current search happening and user is connected to a network THEN
         *
         * Determine if there are any results and store that in the searchFoundResults variable (true/ false)
         * Wait until that completes (jobDownloadData.join()) and if results found -> show them the results on Map
         * otherwise display a dialog that no results were found
         */
        if (!currentlySearching) {
            try {
                val url = URL(serverLocation)
                if (isNetworkConnected()) {
                    currentlySearching = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val jobDownloadData = CoroutineScope(Dispatchers.IO).launch {
                            val downloadData = DownloadData(url, this@MainActivity)
                            val result: Deferred<Boolean> = async {
                                downloadData.startDownload()
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
            } catch (e: Exception) {
                Log.d(TAG, "error in searching ${e.message}")
            }
        }
    }

    override fun searchTimedOut() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "MAIN ACTIVITY: Searched Time out")
            showDialog(R.string.searchError, R.string.searchErrorDescription, R.string.ok)
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return cm.activeNetwork != null
        }
        return cm.activeNetworkInfo != null
    }

    override fun useCurrentLocationButtonPressed(simpleSearch: Boolean) {
        this.simpleSearch = simpleSearch
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissionDialog()
            return
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        Log.d(TAG, "gps provider enabled ${locationManager.isProviderEnabled("gps")}")
        //todo: maybe add internet or other providers to this
        if (!locationManager.isProviderEnabled("gps")) {
            showDialogOnMainThread(R.string.locationOffTitle, R.string.locationOffDescription, R.string.ok)
            return
        }

        val frag = getFrag()
        if (frag != null && frag is SearchFragment) {
            frag.setAddressValue(getString(R.string.yourLocation))
        }

        locationManager.requestLocationUpdates("gps", 100, 50F, object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                if (location != null) {
                    Log.d(TAG, "lat is ${location.latitude}, long is ${location.longitude}")
                    updateEditTextWithLocation(location.latitude.toString(), location.longitude.toString())


                    Log.d(TAG, "frag is $frag")
                    if (frag is SearchFragment) {
                        Log.d(TAG, "YAY! Frag is Search Frag")
                        frag.setLatLngTextViews(location.latitude.toString(), location.longitude.toString())

                        //automatically start searching if simpleSearch
                        if (simpleSearch) {
                            searchButtonPressed(location.latitude, location.longitude, DEFAULT_SEARCH_RADIUS, false)
                        }
                        locationManager.removeUpdates(this)
                    }
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
            }
        })
    }

    private fun getFrag(): Fragment? {
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermission Callback")
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION_REQUEST_CODE) {
            //clicked accept
            //todo: need to store value for simple search somehow
            useCurrentLocationButtonPressed(simpleSearch)
        } else {
            //clicked deny
            Log.d(TAG, "Permission Denied in Callback")
            //grantAccessSnackbar()
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                grantAccessSnackbar()
            }
        }
    }

    private fun requestLocationPermissionDialog() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_REQUEST_CODE
        )
    }

    private fun grantAccessSnackbar() {
        Snackbar.make(findViewById(R.id.constraintLayout), R.string.locationSnackbar, Snackbar.LENGTH_LONG)
            .setAction(R.string.grantAccess) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", this.packageName, null)
                    startActivity(intent)
            }
            .show()
    }

    private fun updateEditTextWithLocation(lat: String, long: String) {
        editTextLat.setText(lat)
        editTextLon.setText(long)
    }

    override fun selectOnMapButtonPressed() {
        navController.navigate(R.id.selectOnMapFragment)
    }

    private fun launchMapOnMainThread(lat: Double, long: Double, radius: Double, navigationActionID: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            showMap(navigationActionID)
            mainViewModel.searchCenter.value = LatLng(lat, long)
            mainViewModel.searchRadius.value = radius
            val frag = getFrag()
            if (frag != null && frag is SearchFragment) {
                mainViewModel.searchString.value = frag.getAddressValue()
            }
        }
    }

    private fun showMap(navigationActionID: Int) {
        //navController.navigate(R.id.action_navigation_home_to_mapFragment2)
        navController.navigate(navigationActionID)
    }

    private fun launchMapNoAnimation() {
        navController.navigate(R.id.action_navigation_search_to_resultsHolderFragment, null, NavOptions.Builder().setEnterAnim(R.anim.blank_animation).setExitAnim(R.anim.blank_animation)
            .setPopEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_right).build())
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
