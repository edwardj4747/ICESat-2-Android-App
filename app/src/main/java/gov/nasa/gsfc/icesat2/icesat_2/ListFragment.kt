package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "ListFragment"

class ListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val allPointsOneList = ArrayList<Point>()
        MainActivity.getMainViewModel()?.getAllPointsChain()?.observe(viewLifecycleOwner, Observer {
            for (i in 0 until it.size) {
                allPointsOneList.addAll(it[i])
            }
            Log.d(TAG, "allPointsoneList is $allPointsOneList")
        })

        val listRecyclerViewAdapter = ListRecyclerViewAdapter(allPointsOneList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = listRecyclerViewAdapter
    }

}
