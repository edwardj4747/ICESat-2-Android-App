package gov.nasa.gsfc.icesat2.icesat_2.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_gallery.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "GalleryFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryFragment : Fragment(), View.OnClickListener {
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
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    //TODO: resize the image to have multiple version

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        imageView1.setOnClickListener(this)
        imageView2.setOnClickListener(this)
        imageView3.setOnClickListener(this)
        imageView4.setOnClickListener(this)
        imageView5.setOnClickListener(this)
        imageView6.setOnClickListener(this)
        imageView7.setOnClickListener(this)
        imageView8.setOnClickListener(this)
        imageView9.setOnClickListener(this)
        imageView10.setOnClickListener(this)
        imageView11.setOnClickListener(this)
        imageView12.setOnClickListener(this)
        imageView13.setOnClickListener(this)
        imageView14.setOnClickListener(this)
        imageView15.setOnClickListener(this)
        imageView16.setOnClickListener(this)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GalleryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GalleryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        var index = 0
        when (v?.id) {
            R.id.imageView1 -> index = 0
            R.id.imageView2 -> index = 1
            R.id.imageView3 -> index = 2
            R.id.imageView4 -> index = 3
            R.id.imageView5 -> index = 4
            R.id.imageView6 -> index = 5
            R.id.imageView7 -> index = 6
            R.id.imageView8 -> index = 7
            R.id.imageView9 -> index = 8
            R.id.imageView10 -> index = 9
            R.id.imageView11 -> index = 10
            R.id.imageView12 -> index = 11
            R.id.imageView13 -> index = 12
            R.id.imageView14 -> index = 13
            R.id.imageView15 -> index = 14
            R.id.imageView16 -> index = 15
        }
        //val params = GalleryFragmentDirections.actionNavigationGalleryToGalleryDisplay(titles[index], images[index], descriptions[index], index)


        /*val params = GalleryFragmentDirections.actionNavigationGalleryToGalleryDisplay(index)
        findNavController().navigate(params)*/

        findNavController().navigate(R.id.action_navigation_gallery_to_galleryContainerFragment2)
    }
}