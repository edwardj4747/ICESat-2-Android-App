package gov.nasa.gsfc.icesat2.icesat_2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import kotlinx.android.synthetic.main.activity_main_nav.*
import kotlinx.coroutines.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ISearchFragmentCallback {

    private lateinit var navController: NavController

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

    override fun searchButtonPressed(serverLocation: String, lat:Double, long: Double, radius:Double) {
        Log.d(TAG, "MainActivity: starting download from $serverLocation")


        var searchResultsFound = false

        /**
         * Determine if there are any results and store that in the searchFoundResults variable
         * Wait until that completes (jobDownloadData.join()) and if results found -> show them
         * otherwise display a dialog that no results were found
         */
        CoroutineScope(Dispatchers.IO).launch {
            val jobDownloadData = CoroutineScope(Dispatchers.IO).launch {
                val downloadData = DownloadData()
                val result: Deferred<Boolean> = async { downloadData.startDownload(serverLocation) }
                searchResultsFound = result.await()
            }

            jobDownloadData.join()

            Log.d(TAG, "searchResultsFound = $searchResultsFound")
            if (searchResultsFound) {
                Log.d(TAG, "YAY!! Search results found")
                launchMapOnMainThread(lat, long, radius)
            } else {
                Log.d(TAG, "No Search results found")
                showNoResultsDialogOnMainThread()
            }
        }

    }

    override fun useCurrentLocationButtonPressed() {

    }

    private fun launchMapOnMainThread(lat: Double, long: Double, radius: Double) {
        GlobalScope.launch(Dispatchers.Main) {
            showMap()
            mainViewModel.searchCenter.value = LatLng(lat, long)
            mainViewModel.searchRadius.value = radius
        }

    }

    private fun showMap() {
        navController.navigate(R.id.action_navigation_home_to_mapFragment2)
    }


    private fun showNoResultsDialogOnMainThread() {
        GlobalScope.launch(Dispatchers.Main) {
            showNoResultsDialog()
        }
    }

    private fun showNoResultsDialog() {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setMessage(R.string.noResultsDetails)
            ?.setTitle(R.string.noResults)
            ?.setPositiveButton(R.string.backToSearch) { dialog, which -> Log.d(TAG, "Dialog positive button clicked") }
        alertBuilder.show()
    }


}
