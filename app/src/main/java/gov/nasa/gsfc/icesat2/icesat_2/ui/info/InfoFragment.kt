package gov.nasa.gsfc.icesat2.icesat_2.ui.info

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.YouTubeStandalonePlayer
import gov.nasa.gsfc.icesat2.icesat_2.DATE_RANGE
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_info.*


private const val TAG = "InfoFragment"

class InfoFragment : Fragment() {

    private lateinit var notificationsViewModel: InfoViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //get the current date range
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val dateRange = sharedPref.getString(DATE_RANGE, "")

        Log.d(TAG, "Date Range is $dateRange")

        if (dateRange != "") {
            //there is a dateRange so display it
        }

        textViewWatchVideo.setOnClickListener {
            val intent = YouTubeStandalonePlayer.createVideoIntent(requireActivity(), getString(R.string.google_maps_key), "ybt5Qy4XaNU", 0, true, false)
            startActivity(intent)
        }

        //clicking on links takes you to the appropriate webpage
        textViewInfo.movementMethod = LinkMovementMethod.getInstance()
    }
}
