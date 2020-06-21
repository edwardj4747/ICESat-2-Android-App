package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_results_holder.*

/**
 * A simple [Fragment] subclass.
 */
private const val TAG = "ResultsHolderFragment"

class ResultsHolderFragment : Fragment(), IFavoritesFragmentCallback {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_results_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sectionsPagerAdapter = MapListsPagerAdapter(requireContext(), childFragmentManager, this)
        viewPager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(viewPager)

    }

    override fun navigateToSingleMarkerMap(lat: Double, long: Double, title: String) {
        val params = ResultsHolderFragmentDirections.actionResultsHolderFragmentToSingleMarkerMap(lat.toFloat(), long.toFloat(), title)
        this.findNavController().navigate(params)
    }

}
