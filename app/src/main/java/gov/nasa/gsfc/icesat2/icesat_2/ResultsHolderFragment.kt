package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_results_holder.*
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
private const val TAG = "ResultsHolderFragment"

class ResultsHolderFragment : Fragment() {

    private var tabClickedCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_results_holder, container, false)

        /*val backButtonListener = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val overallNavController = findNavController()
                Log.d(TAG, "back button pressed")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonListener)*/

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = activity?.findNavController(R.id.results_holder_host)


        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabClickedCount++
                when (tab?.position) {
                    0 -> {
                        navController?.navigate(R.id.action_list_fragment_to_map_fragment)
                        //navController?.popBackStack(R.id.action_list_fragment_to_map_fragment, true)
                    }
                    1 -> {
                        navController?.navigate(R.id.action_mapFragment_to_listFragment)

                    }
                }
            }
        })
    }

}
