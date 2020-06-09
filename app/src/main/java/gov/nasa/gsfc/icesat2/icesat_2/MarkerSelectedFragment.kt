package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry
import gov.nasa.gsfc.icesat2.icesat_2.ui.favorites.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_marker_selected.*

private const val TAG = "MarkerSelectedFragment"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [MarkerSelectedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MarkerSelectedFragment : Fragment() {

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var selectedPoint: Point
    private var favoritesEntry: FavoritesEntry? = null //null if no new favorite to add

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedPoint = it.getParcelable<Point>(ARG_PARAM3)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_marker_selected, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favoritesViewModel =
            ViewModelProviders.of(this).get(FavoritesViewModel::class.java)

        textViewDate.text = selectedPoint.dateString
        textViewTime.text = selectedPoint.dateString


        btnFavorite.setOnClickListener {
            if (btnFavorite.tag == "favorite") {
                //remove from favorites
                btnFavorite.setImageResource(R.drawable.ic_star_border_black_24dp)
                btnFavorite.tag = "notFavorite"
                Toast.makeText(requireContext(), "Removed From Favorites", Toast.LENGTH_SHORT).show()
                favoritesEntry = null
            } else {
                btnFavorite.setImageResource(R.drawable.ic_shaded_star_24)
                btnFavorite.tag = "favorite"
                Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_SHORT).show()
                favoritesEntry = FavoritesEntry(selectedPoint.dateObject.time, selectedPoint.dateString, selectedPoint.latitude, selectedPoint.longitude)
            }

        }

        btnClose.setOnClickListener {
            val listener = requireParentFragment() as MapFragment
            listener.closeButtonPressed()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param3: Point) =
            MarkerSelectedFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM3, param3)
                }
            }
    }

    /**
     * If the users starred a location, add it to favorites when this fragment gets closed
     */
    override fun onStop() {
        super.onStop()
        if (favoritesEntry != null) {
            //no favorite is in the database with this timestamp
            if (!favoritesViewModel.contains(favoritesEntry!!.dateObjectTime)) {
                Log.d(TAG, "entry is NOT in favorites")
                favoritesViewModel.insert(favoritesEntry!!)
            } else {
                Log.d(TAG, "entry IS in favorites")
            }
        }
    }
}