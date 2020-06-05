package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_results_holder.*

/**
 * A simple [Fragment] subclass.
 */
private const val TAG = "ResultsHolderFragment"

class ResultsHolderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_results_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sectionsPagerAdapter = MapListsPagerAdapter(requireContext(), childFragmentManager)
        viewPager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(viewPager)
    }

}
