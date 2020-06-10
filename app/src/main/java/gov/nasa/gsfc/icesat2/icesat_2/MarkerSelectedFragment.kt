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
    //one of these two will always be null because this is for one favorite entry and cannot both add and remove it
    private var favoritesEntryToAdd: FavoritesEntry? = null //null if no new favorite to add
    private var favoritesEntryToRemove: FavoritesEntry? = null //null if no favorite to remove

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


        if (entryInDatabase(FavoritesEntry(selectedPoint.dateObject.time, selectedPoint.dateString, selectedPoint.latitude, selectedPoint.longitude))) {
            btnFavorite.setImageResource(R.drawable.ic_shaded_star_24)
            btnFavorite.tag = "favorite"
        }

        btnFavorite.setOnClickListener {
            if (btnFavorite.tag == "favorite") {
                //remove from favorites
                btnFavorite.setImageResource(R.drawable.ic_star_border_black_24dp)
                btnFavorite.tag = "notFavorite"
                Toast.makeText(requireContext(), "Removed From Favorites", Toast.LENGTH_SHORT).show()
                favoritesEntryToAdd = null
                favoritesEntryToRemove = FavoritesEntry(selectedPoint.dateObject.time, selectedPoint.dateString, selectedPoint.latitude, selectedPoint.longitude)
            } else {
                btnFavorite.setImageResource(R.drawable.ic_shaded_star_24)
                btnFavorite.tag = "favorite"
                Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_SHORT).show()
                favoritesEntryToAdd = FavoritesEntry(selectedPoint.dateObject.time, selectedPoint.dateString, selectedPoint.latitude, selectedPoint.longitude)
                favoritesEntryToRemove = null
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

        if (favoritesEntryToAdd != null && !entryInDatabase(favoritesEntryToAdd!!)) {
            Log.d(TAG, "entry is NOT in favorites. Adding it")
            favoritesViewModel.insert(favoritesEntryToAdd!!)
        } else if (favoritesEntryToRemove != null) {
            Log.d(TAG, "Entry was previously entered in favorites. Now removing it")
            favoritesViewModel.delete(favoritesEntryToRemove!!.dateObjectTime)
        }

    }

    private fun entryInDatabase(favEntry: FavoritesEntry) : Boolean {
            return favoritesViewModel.contains(favEntry.dateObjectTime)
    }
}