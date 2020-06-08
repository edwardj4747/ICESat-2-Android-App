package gov.nasa.gsfc.icesat2.icesat_2.ui.favorites

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import gov.nasa.gsfc.icesat2.icesat_2.FavoritesAdapter
import gov.nasa.gsfc.icesat2.icesat_2.R
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry
import kotlinx.android.synthetic.main.fragment_favorite.*

private const val TAG = "FavoritesFragment"

class FavoritesFragment : Fragment() {

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var favoritesList: List<FavoritesEntry>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        favoritesViewModel =
                ViewModelProviders.of(this).get(FavoritesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_favorite, container, false)

        favoritesViewModel.getAllFavorites().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "allFavorites $it")
            favoritesList = it
            initializeRecyclerView()
        })

        setHasOptionsMenu(true)
        return root
    }

   private fun initializeRecyclerView() {
       val adapter = FavoritesAdapter(favoritesList)
      favoriteRecyclerView.adapter = adapter
       favoriteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
   }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuDelete -> {
                showDialog(getString(R.string.deleteAllTitle), getString(R.string.deleteAllDescription), getString(R.string.yes), getString(R.string.cancel))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialog(title: String, description: String, positive: String, negative: String) {
        val alertBuilder = AlertDialog.Builder(requireContext())
        alertBuilder.setMessage(description)
            ?.setTitle(title)
            ?.setPositiveButton(positive) { dialog, which ->
                favoritesViewModel.deleteAll()
            }
            ?.setNegativeButton(negative) {dialog, which ->  }
        alertBuilder.show()
    }
}
