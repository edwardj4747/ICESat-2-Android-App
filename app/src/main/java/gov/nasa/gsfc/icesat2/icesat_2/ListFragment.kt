package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "ListFragment"

class ListFragment : Fragment(), IFavoritesFragmentCallback {
    private lateinit var listener: IFavoritesFragmentCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*val allPointsOneList = ArrayList<Point>()
        MainActivity.getMainViewModel()?.getAllPointsChain()?.observe(viewLifecycleOwner, Observer {
            for (i in 0 until it.size) {
                allPointsOneList.addAll(it[i])
            }
            Log.d(TAG, "allPointsoneList is $allPointsOneList")
        })*/

        var allPointsOneList = ArrayList<Point>()
        MainActivity.getMainViewModel()?.getAllPointsList()?.observe(viewLifecycleOwner, Observer {
            allPointsOneList = it
            Log.d(TAG, "allPointsList observer called \n $allPointsOneList")
            setUpRecyclerView(allPointsOneList)
        })
    }

    private fun setUpRecyclerView(allPointsOneList: ArrayList<Point>?) {
        Log.d(TAG, "setting up recycler view")
        val listRecyclerViewAdapter = ListRecyclerViewAdapter(requireContext(), allPointsOneList!!)
        listRecyclerViewAdapter.setUpListener(this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = listRecyclerViewAdapter
    }

    fun setUpListener(listener: IFavoritesFragmentCallback) {
        this.listener = listener
    }

    override fun navigateToSingleMarkerMap(lat: Double, long: Double, title: String) {
        listener.navigateToSingleMarkerMap(lat, long, title)
    }
}
