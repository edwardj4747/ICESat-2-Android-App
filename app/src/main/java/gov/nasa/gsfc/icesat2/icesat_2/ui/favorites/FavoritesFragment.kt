package gov.nasa.gsfc.icesat2.icesat_2.ui.favorites

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import gov.nasa.gsfc.icesat2.icesat_2.FavoritesAdapter
import gov.nasa.gsfc.icesat2.icesat_2.ILaunchSingleMarkerMap
import gov.nasa.gsfc.icesat2.icesat_2.R
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry
import kotlinx.android.synthetic.main.fragment_favorite.*

private const val TAG = "FavoritesFragment"

class FavoritesFragment : Fragment(), ILaunchSingleMarkerMap {

    private lateinit var favoritesViewModel: FavoritesViewModel
    //private lateinit var favoritesList: List<FavoritesEntry>
    private lateinit var favoritesList: ArrayList<FavoritesEntry>
    private var swipeListenerAttached = false
    private var recyclerViewInitialized = false
    private lateinit var localFavoritesList: ArrayList<FavoritesEntry>
    private val localDeletedFavorites = ArrayList<Long>() //to keep track of which favorites were deleted and used to update database on stop

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        favoritesViewModel =
                ViewModelProviders.of(this).get(FavoritesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_favorite, container, false)

      /*  favoritesViewModel.getAllFavorites().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "allFavorites size is ${it.size}: $it")
            favoritesList = it
            initializeRecyclerView()
        })*/

        favoritesViewModel.getAllFavorites().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "OBSERVED. size is ${it.size}")
            favoritesList = it as ArrayList<FavoritesEntry>

            initRV()
        })

        setHasOptionsMenu(true)
        return root
    }

    private fun initRV() {

        val adapter = FavoritesAdapter(requireContext(), favoritesList)
        adapter.setListener(this)
        favoriteRecyclerView.adapter = adapter
        favoriteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        displayNoFavoritesTextIfNecessary()


            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    try {
                        val pos = viewHolder.adapterPosition
                        val element = favoritesList[pos]
                        favoritesViewModel.delete(element.dateObjectTime)

                        Snackbar.make(snackbarCoordinator, R.string.itemDeleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo) {
                                favoritesViewModel.insert(element)
                                displayNoFavoritesTextIfNecessary()
                            }
                            .show()

                        /*Log.d(TAG, "pos is $pos")
                        val deletedFavorite = favoritesList.removeAt(pos)
                        localDeletedFavorites.add(deletedFavorite.dateObjectTime)

                        adapter.notifyItemRemoved(pos)
                        displayNoFavoritesTextIfNecessary()
                        Log.d(TAG, "After removing, new size is ${favoritesList.size}")
                        Snackbar.make(snackbarCoordinator, R.string.itemDeleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo) {
                                favoritesList.add(pos, deletedFavorite)
                                localDeletedFavorites.removeAt(localDeletedFavorites.size - 1)
                                adapter.notifyItemInserted(pos)
                                displayNoFavoritesTextIfNecessary()
                            }
                            .show()*/
                    } catch (e: Exception) { Log.d(TAG, "error in onSwiped ${e.message}") }
                }
            }).attachToRecyclerView(favoriteRecyclerView)
    }

   /*private fun initializeRecyclerView() {
       recyclerViewInitialized = true
       //todo: this is kind of inefficient
       localFavoritesList = ArrayList(favoritesList)

       displayNoFavoritesTextIfNecessary()

       val adapter = FavoritesAdapter(requireContext(), localFavoritesList)
       adapter.setListener(this)
       favoriteRecyclerView.adapter = adapter
       favoriteRecyclerView.layoutManager = LinearLayoutManager(requireContext())


           *//*Log.d(TAG, "Attaching swipe listener")
           swipeListenerAttached = true*//*
           //delete swiping
           ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
               override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                   return false
               }

               override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                   try {
                       val deletedFavoritePosition = viewHolder.adapterPosition
                       val deletedFavorite = localFavoritesList[deletedFavoritePosition]
                       //val deletedFavorite = adapter.getFavoriteAt(viewHolder.adapterPosition)
                       //favoritesViewModel.delete(deletedFavorite.dateObjectTime)
                       //Snackbar.make(this@FavoritesFragment.requireView(), R.string.itemDeleted, Snackbar.LENGTH_LONG)
                       Snackbar.make(snackbarCoordinator, R.string.itemDeleted, Snackbar.LENGTH_LONG)
                           .setAction(R.string.undo) {
                               //favoritesViewModel.insert(deletedFavorite)
                               localDeletedFavorites.remove(deletedFavorite.dateObjectTime)
                               localFavoritesList.add(deletedFavoritePosition, deletedFavorite)
                               adapter.notifyItemInserted(deletedFavoritePosition)
                               displayNoFavoritesTextIfNecessary()
                           }
                           .show()
                       localDeletedFavorites.add(deletedFavorite.dateObjectTime)
                       localFavoritesList.removeAt(viewHolder.adapterPosition)
                       adapter.notifyItemRemoved(viewHolder.adapterPosition)
                       displayNoFavoritesTextIfNecessary()
                   } catch (e: Exception) {
                       Log.d(TAG, "error in onswiped ${e.message}")
                       Log.d(TAG, "${e.stackTrace}")
                   }

               }
           }).attachToRecyclerView(favoriteRecyclerView)


   }*/

    private fun displayNoFavoritesTextIfNecessary() {
        if (favoritesList.isEmpty()) {
            textViewNoFavorites.visibility = View.VISIBLE
        } else {
            textViewNoFavorites.visibility = View.INVISIBLE
        }
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


    //delete items from the database
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "number of favorites to remove is ${localDeletedFavorites.size}")
        for (i in 0 until localDeletedFavorites.size) {
            favoritesViewModel.delete(localDeletedFavorites[i])
        }
    }

    //navigates from favorites adapter to single marker map
    override fun navigateToSingleMarkerMap(lat: Double, long: Double, title: String, dateObjectTime: Long) {
        Log.d(TAG, "navigateSingleMarkerMap called \n $title")
        val params = FavoritesFragmentDirections.actionNavigationFavoritesToSingleMarkerMap(lat.toFloat(), long.toFloat(), title, dateObjectTime)
        this.findNavController().navigate(params)
    }
}
