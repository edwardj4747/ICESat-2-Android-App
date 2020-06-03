package gov.nasa.gsfc.icesat2.icesat_2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import kotlinx.android.synthetic.main.activity_main_nav.*
import kotlinx.coroutines.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ISearchFragmentCallback {

    private val fragmentManger = supportFragmentManager

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

       /* val navController = this.findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)*/


        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        /*//created for the first time
        if (savedInstanceState == null) {
            val fragmentTransaction = fragmentManger.beginTransaction()
            val searchFragment = SearchFragment()
            searchFragment.addSearchFragmentCallbackListener(this) // important!!
            fragmentTransaction.apply {
                replace(R.id.fragmentContainer, searchFragment)
                commit()
            }
        } else {
            var currFrag = fragmentManger.findFragmentById(R.id.fragmentContainer)
            if (currFrag != null) {
                currFrag = currFrag as SearchFragment
                currFrag.addSearchFragmentCallbackListener(this)
            } else {
                Log.d(TAG, "current frag is null so the listener could not be reattached")
            }
        }*/

        /*val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)*/
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
                showNoResultsDialogOnMainThread()
            }
        }

    }

    override fun useCurrentLocationButtonPressed() {
        //testing adding a new Fragment over top
        Log.d(TAG, "MainActivity: Replacing search fragment starts")
        /*val newFrag = SearchFragment()
        newFrag.addSearchFragmentCallbackListener(this)
        val fragmentTransaction = fragmentManger.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.fragmentContainer, newFrag)
            commit()
        }*/

        Log.d(TAG, "MainActivity: replacing searchFragment ends")
    }

    private fun launchMapOnMainThread(lat: Double, long: Double, radius: Double) {
        GlobalScope.launch(Dispatchers.Main) {
            showMap()
            mainViewModel.searchCenter.value = LatLng(lat, long)
            mainViewModel.searchRadius.value = radius
        }

    }

    private fun showMap() {
        val mapFragment = MapFragment()
        val fragmentTransaction = fragmentManger.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.fragmentContainer, mapFragment)
            addToBackStack(null)
            commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
        return true
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
