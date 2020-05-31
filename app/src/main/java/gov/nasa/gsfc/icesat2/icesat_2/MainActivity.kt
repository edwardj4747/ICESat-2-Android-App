package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.SearchFragment


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ISearchFragmentCallback {

    private val fragmentManger = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //created for the first time
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

    override fun searchButtonPressed(serverLocation: String) {
        Log.d(TAG, "MainActivity: starting download from $serverLocation")
        val downloadData = DownloadData()
        downloadData.startDownload(serverLocation)
    }




}
