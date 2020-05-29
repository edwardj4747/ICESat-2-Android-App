package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.ISearchFragmentCallback
import gov.nasa.gsfc.icesat2.icesat_2.ui.search.SearchFragment
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"

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
        val downloadData = DownloadData()
        downloadData.startDownload("http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=-38.9&lon=78.1&r=25&u=miles")
        Log.d(TAG, "MainActivity: SearchButtonPressed ends")
    }
}
