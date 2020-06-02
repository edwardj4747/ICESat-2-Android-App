package gov.nasa.gsfc.icesat2.icesat_2.ui.search

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import gov.nasa.gsfc.icesat2.icesat_2.*
import kotlinx.android.synthetic.main.fragment_search.*
import java.lang.Exception


private const val TAG = "SearchFragment"
private const val LAT_INPUT_ERROR = "Please Enter Latitude between -86.0 and 86.0 ${0x00B0.toChar()}N"
private const val LONG_INPUT_ERROR = "Please Enter Longitude between -180.0 and 180.0 ${0x00B0.toChar()}E"
private const val RADIUS_INPUT_ERROR_MILES = "Please Enter Radius between 1.1 and 25.0 Miles"
private const val RADIUS_INPUT_ERROR_KILOMETERS = "Please Enter Radius between 1.1 and 40.2 Kilometers"

class SearchFragment : Fragment() {

    private lateinit var listener: ISearchFragmentCallback
    private var viewModel: MainViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (MainActivity.getMainViewModel() != null) {
            Log.d(TAG, "********************")
            Log.d(TAG, "SearchFragment assigning viewModel to MainActivity View Model")
            viewModel = MainActivity.getMainViewModel()

            viewModel?.getAllPointsList()?.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "SearchFragment value of allPoints is $it")
                Log.d(TAG, "size of allPoints is ${it.size}")
                try {
                    updateTextView(it[0].toString())
                } catch (e: Exception) {
                    Log.d(TAG, "SearchFragment no values in all Points array")
                }

            })

            viewModel?.getAllPointsChain()?.observe(viewLifecycleOwner, Observer {
                /*Log.d(TAG, "=======Split into Chains Array===========")
                Log.d(TAG, "number of chains ${it.size}")
                for (i in 0 until it.size) {
                    Log.d(TAG, "chain $i. size of chain ${it[i].size}: ${it[i]}")
                }*/
            })
        }

        btnSearch.setOnClickListener {
            Log.d(TAG, "SearchFragment: Search Button Pressed")

            val inputs = allInputsValid() //returns array of {lat, long, radius} if valid. null if not valid
            if (inputs != null) {
                val unit = if (unitSpinner.selectedItem.toString() == "Kilometers") {
                    "kilometers"
                } else {
                    "miles"
                }
                //http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=-38.9&lon=78.1&r=25&u=miles
                val serverLocation = "http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=${inputs[0]}&lon=${inputs[1]}&r=${inputs[2]}&u=$unit"
                listener.searchButtonPressed(serverLocation)
            }
            Log.d(TAG, "SearchFragment: SearchButtonPressed ends")
        }

        btnUseCurrentLoc.setOnClickListener {
            listener.useCurrentLocationButtonPressed()
        }


        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.unitSelector, android.R.layout.simple_spinner_dropdown_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter

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

        val radiusSelection = unitSpinner.selectedItem.toString()
        //No radius entered
        if (radiusSelection == "Miles" && editTextRadius.text.toString() == "") {
            createSnackBar(RADIUS_INPUT_ERROR_MILES)
            return null
        } else if (radiusSelection == "Kilometers" && editTextRadius.text.toString() == "") {
            createSnackBar(RADIUS_INPUT_ERROR_KILOMETERS)
            return null
        }

        //invalid value for radius
        val radius = editTextRadius.text.toString().toDouble()
        if (radiusSelection == "Miles" && radius < 1.1 || radius > 25) {
            createSnackBar(RADIUS_INPUT_ERROR_MILES)
            return null
        } else if (radiusSelection == "Kilometers" && radius < 1.1 || radius > 40.2) {
            createSnackBar(RADIUS_INPUT_ERROR_KILOMETERS)
            return null
        }
        return doubleArrayOf(lat, long, radius)
    }

    private fun createSnackBar(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction("OK") {  }
                .setBackgroundTint(resources.getColor(R.color.snackbarColor, null))
                .show()
        } else {
            Snackbar.make(requireActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction("OK") {  }
                .setBackgroundTint(resources.getColor(R.color.snackbarColor))
                .show()
        }
    }

    private fun updateTextView(text: String) {
        textViewSearch.text = text
    }

    fun addSearchFragmentCallbackListener(theListener: ISearchFragmentCallback) {
        listener = theListener
    }


}
