package gov.nasa.gsfc.icesat2.icesat_2.ui.info

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_info.*

class InfoFragment : Fragment() {

    private lateinit var notificationsViewModel: InfoViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        /*notificationsViewModel =
                ViewModelProviders.of(this).get(InfoViewModel::class.java)*/
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textViewInfo.movementMethod = LinkMovementMethod.getInstance()
    }
}
