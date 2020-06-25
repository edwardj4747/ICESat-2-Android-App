package gov.nasa.gsfc.icesat2.icesat_2.ui.search

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import gov.nasa.gsfc.icesat2.icesat_2.DEFAULT_SEARCH_RADIUS
import gov.nasa.gsfc.icesat2.icesat_2.MainActivity
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_search.*


private const val TAG = "SearchFragment"
private const val AUTOCOMPLETE_REQUEST_CODE = 1;
private const val LAT_INPUT_ERROR = "Please Enter Latitude between -86.0 and 86.0 ${0x00B0.toChar()}N"
private const val LONG_INPUT_ERROR = "Please Enter Longitude between -180.0 and 180.0 ${0x00B0.toChar()}E"
private const val RADIUS_INPUT_ERROR_MILES = "Please Enter Radius between 1.1 and 25.0 Miles"
private const val RADIUS_INPUT_ERROR_KILOMETERS = "Please Enter Radius between 1.1 and 40.2 Kilometers"

private lateinit var navController:NavController

class SearchFragment : Fragment() {

    private lateinit var listener: ISearchFragmentCallback
    private var address: String? = null
    private var simpleSearch = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        btnSelectOnMap.setOnClickListener {
            navController.navigate(R.id.action_navigation_home_to_mapFragment2)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //register the listener to be the parent activity every time the fragment is created. Gives access to all the methods in [ISearchFragmentCallback]
        listener = requireActivity() as MainActivity

        //will display a menu
        setHasOptionsMenu(true)

        btnTrack.setOnClickListener {
            Toast.makeText(requireContext(), "Hopefully able to show current location soon", Toast.LENGTH_SHORT).show()
        }

        btnSearch.setOnClickListener {
            val inputs = allInputsValid() //returns array of {lat, long, radius} if valid. null if not valid
            if (inputs != null) {
                //http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=-38.9&lon=78.1&r=25&u=miles
                //val serverLocation = "http://icesat2app-env.eba-gvaphfjp.us-east-1.elasticbeanstalk.com/find?lat=${inputs[0]}&lon=${inputs[1]}&r=${inputs[2]}&u=miles"
                listener.searchButtonPressed(inputs[0], inputs[1], inputs[2], false)
            }
        }

        btnUseCurrentLoc.setOnClickListener {
            listener.useCurrentLocationButtonPressed(simpleSearch)
        }

        btnSelectOnMap.setOnClickListener {
            listener.selectOnMapButtonPressed()
        }

        btnUseSearchBar.setOnClickListener {
            useSearchBar()
        }

        textViewAdvancedSearch.setOnClickListener {
            /*if (editTextLat.visibility == View.GONE) {
                textViewAdvancedSearch.text = getString(R.string.simpleSearch)

                editTextLat.visibility = View.VISIBLE
                editTextLon.visibility = View.VISIBLE
                textViewEnterLatLng.visibility = View.VISIBLE
                unitSpinner.visibility = View.VISIBLE
                editTextRadius.visibility = View.VISIBLE
            } else {
                textViewAdvancedSearch.text = getString(R.string.advancedSearch)

                editTextLat.visibility = View.GONE
                editTextLon.visibility = View.GONE
                textViewEnterLatLng.visibility = View.GONE
                unitSpinner.visibility = View.GONE
                editTextRadius.visibility = View.GONE
            }*/

            //hide advanced search text + show advanced search fields
            simpleSearch = false
            textViewAdvancedSearch.visibility = View.GONE
            //btnSearch2.visibility = View.GONE

            textViewSimpleSearch.visibility = View.VISIBLE
            editTextLat.visibility = View.VISIBLE
            editTextLon.visibility = View.VISIBLE
            //textViewEnterLatLng.visibility = View.VISIBLE
            unitSpinner.visibility = View.VISIBLE
            editTextRadius.visibility = View.VISIBLE
            btnSearch.visibility = View.VISIBLE
        }

        textViewSimpleSearch.setOnClickListener {
            simpleSearch = true
            textViewAdvancedSearch.visibility = View.VISIBLE
            //btnSearch2.visibility = View.VISIBLE
            textViewSimpleSearch.visibility = View.GONE

            editTextLat.visibility = View.GONE
            editTextLon.visibility = View.GONE
            //textViewEnterLatLng.visibility = View.GONE
            unitSpinner.visibility = View.GONE
            editTextRadius.visibility = View.GONE
            btnSearch.visibility = View.GONE
        }


        editTextLat.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                address = "custom"
                setAddressTextView()
            }
        }

        editTextLon.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                address = "custom"
                setAddressTextView()
            }
        }



        //will set the search location if appropriate
        setAddressTextView()

        //set up the spinner
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.unitSelector, android.R.layout.simple_spinner_dropdown_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter

        //initialize dependencies and such to have an address searcher
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        val placesClient = Places.createClient(requireContext())

        /*//TODO: Look into search billing + remove this and UI component if not useful
        //searching
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setHint(getString(R.string.searchForLocation))

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d(TAG, "Place: " + place.getName());
                Log.d(TAG, "Lat Lng is ${place.latLng}")
                Log.d(TAG, "Address is ${place.address}")
                setLatLngTextViews(place.latLng)
            }

            override fun onError(status: Status) {
                Log.d(TAG, "An error occurred: " + status);
            }
        })*/

    }

    fun setLatLngTextViews(lat: String, long: String) {
        editTextLat.setText(lat)
        editTextLon.setText(long)
    }

    fun clearLatLngTextViews() {
        editTextLat.setText("")
        editTextLon.setText("")
        editTextRadius.setText("")
    }

    private fun setLatLngTextViews(value: LatLng?) {
        if (value != null) {
            setLatLngTextViews(value.latitude.toString(), value.longitude.toString())
            /*editTextLat.setText(value.latitude.toString())
            editTextLon.setText(value.longitude.toString())*/
        } else {
            Toast.makeText(requireContext(), "Error Occured", Toast.LENGTH_LONG).show()
        }
    }

    private fun setAddressTextView() {
        if (address != null && address != "") {
            textViewEnterLocation.visibility = View.INVISIBLE
            textViewAdress.visibility = View.VISIBLE
            textViewAdress.text = "Searching for: $address"
        } else {
            textViewAdress.text = ""
            textViewEnterLocation.visibility = View.VISIBLE
            textViewAdress.visibility = View.INVISIBLE
        }
    }

    fun setAddressValue(newValue: String) {
        address = newValue
        setAddressTextView()
    }

    fun getAddressValue(): String? = address

    //return null if there is an error with one of the inputs. Otherwise return array of {lat, lng, radius}
    //NOTE RADIUS can be entered in kilometers but will be converted immediately into miles to make for seamless use
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
        var radius = editTextRadius.text.toString().toDouble()
        if (radiusSelection == "Miles" && (radius < 1.1 || radius > 25)) {
            createSnackBar(RADIUS_INPUT_ERROR_MILES)
            return null
        } else if (radiusSelection == "Kilometers" && (radius < 1.1 || radius > 40.2)) {
            createSnackBar(RADIUS_INPUT_ERROR_KILOMETERS)
            return null
        }

        //valid radius in kilometers -> convert it to miles
        if (radiusSelection == "Kilometers") {
            val KILO_TO_MILES = 0.621371
            radius *= KILO_TO_MILES
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_frag_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuClearSearch -> {
                address = ""
                setAddressTextView()
                clearLatLngTextViews()
            }

            R.id.menuSearch -> {
                Log.d(TAG, "Search menu button pressed")
                useSearchBar()
            }
        }
        return true
    }

    private fun useSearchBar() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (data != null) {
                when (resultCode) {
                    RESULT_OK -> {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Log.i(TAG, "Place: " + place.name)
                        val latLng = place.latLng
                        setLatLngTextViews(latLng)
                        address = place.name
                        setAddressTextView()
                        if (place.latLng != null && simpleSearch) {
                            listener.searchButtonPressed(place.latLng!!.latitude, place.latLng!!.longitude, DEFAULT_SEARCH_RADIUS, false)
                        } else {
                            editTextRadius.requestFocus()
                        }
                    }
                    AutocompleteActivity.RESULT_ERROR -> {
                        // TODO: Handle the error.
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, "${status.statusMessage}")
                    }
                    RESULT_CANCELED -> {
                        // The user canceled the operation.
                    }
                }
            } else {
                Log.d(TAG, "intent is equal to null")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
