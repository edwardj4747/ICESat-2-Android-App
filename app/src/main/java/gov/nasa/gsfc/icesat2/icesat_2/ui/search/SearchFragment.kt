package gov.nasa.gsfc.icesat2.icesat_2.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_search.*


const val TAG = "SearchFragment"
class SearchFragment : Fragment() {

    lateinit var listener: ISearchFragmentCallback

    companion object {
        private lateinit var searchViewModel: SearchViewModel

        fun getSearchViewModel(): SearchViewModel? {
            if (this::searchViewModel.isInitialized) {
                return searchViewModel
            } else {
                return null
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        val textView: TextView = root.findViewById(R.id.textViewSearch)
        searchViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //searchViewModel.allPointsList.value = Point.allPoints
        searchViewModel.getAllPointsList().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "AllPoints List Changed \n $it")
            Log.d(TAG, "length is ${it.size}")
            if (it.size > 0) {
                updateTextView(it[0].toString())
            } else {
                Log.d(TAG, "No Passovers for this location")
            }
        })

        btnSearch.setOnClickListener {
            listener.searchButtonPressed()
        }

    }

    private fun updateTextView(text: String) {
        textViewSearch.text = text
    }

    fun addSearchFragmentCallbackListener(theListener: ISearchFragmentCallback) {
        listener = theListener
    }
}
