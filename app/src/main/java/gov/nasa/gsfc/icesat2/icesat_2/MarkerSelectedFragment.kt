package gov.nasa.gsfc.icesat2.icesat_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_marker_selected.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MarkerSelectedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MarkerSelectedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var dateString: String = ""
    private var timeString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dateString = it.getString(ARG_PARAM1)!!
            timeString = it.getString(ARG_PARAM2)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_marker_selected, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        textViewDate.text = dateString
        textViewTime.text = timeString

        btnFavorite.setOnClickListener {
            Toast.makeText(requireContext(), "Need to Add to Favorites", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener {
            val listener = requireParentFragment() as MapFragment
            listener.closeButtonPressed()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MarkerSelectedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MarkerSelectedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}