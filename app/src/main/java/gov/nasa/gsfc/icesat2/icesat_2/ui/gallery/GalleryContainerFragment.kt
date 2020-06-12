package gov.nasa.gsfc.icesat2.icesat_2.ui.gallery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_gallery_container.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class GalleryPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        /*var index = 0
        when (position) {
            0 -> index = 0
            1 -> index = 1
            2 -> index = 2
            3 -> index = 3
            4 -> index = 4
            5 -> index = 5
            6 -> index = 6
            7 -> index = 7
            8 -> index = 8
            9 -> index = 9
            10 -> index = 10
            11 -> index = 11
            12 -> index = 12
            13 -> index = 13
            14 -> index = 14
            15 -> index = 15
        }*/
        return GalleryDisplay.newInstance(position)
    }


    override fun getCount(): Int {
        // Show 2 total pages.
        return 16
    }
}


/**
 * A simple [Fragment] subclass.
 * Use the [GalleryContainerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryContainerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery_container, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val galleryPagerAdapter = GalleryPagerAdapter(requireContext(), childFragmentManager)
        galleryViewPager.adapter = galleryPagerAdapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GalleryContainerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GalleryContainerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}