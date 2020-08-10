package gov.nasa.gsfc.icesat2.icesat_2.ui.info

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.YouTubeStandalonePlayer
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

        textViewDateRange.text = getString(R.string.currentData, "Date 1, 2020 - Date 2, 2020")

        textViewWatchVideo.setOnClickListener {
            val intent = YouTubeStandalonePlayer.createVideoIntent(requireActivity(), getString(R.string.google_maps_key), "VTVXrnuvGzU", 0, true, false)
            startActivity(intent)
        }

        //clicking on links takes you to the appropriate webpage
        textViewInfo1.movementMethod = LinkMovementMethod.getInstance()
        textViewInfo2.movementMethod = LinkMovementMethod.getInstance()
        textViewInfo3.movementMethod = LinkMovementMethod.getInstance()
        textViewInfo4.movementMethod = LinkMovementMethod.getInstance()
    }
}
