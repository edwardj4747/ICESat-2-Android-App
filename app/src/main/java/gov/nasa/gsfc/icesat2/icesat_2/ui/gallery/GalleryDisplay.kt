package gov.nasa.gsfc.icesat2.icesat_2.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_gallery_display.*



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val Index_Param = "param1"


private const val TAG = "GalleryDisplay"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryDisplay.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryDisplay : Fragment() {
    // TODO: Rename and change types of parameters
    private var index: Int = 0

    private val args: GalleryDisplayArgs by navArgs()
    //private var index: Int = 1
    private val titles = arrayOf("Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8",
        "Title 9", "Title 10", "Title 11", "Title 12", "Title 13", "Title 14", "Title 15", "Title 16")
    private val descriptions = arrayOf("Description 1", "Description 2", "Description 3", "Description 4", "Description 5", "Description 6", "Description 7", "Description 8",
        "Description 9", "Description 10", "Description 11", "Description 12", "Description 13", "Description 14", "Description 15", "Description 16")
    private val images = arrayOf(R.drawable.icesatc, R.drawable.img_two_c, R.drawable.img_two_c, R.drawable.icesatc,
        R.drawable.icesatc, R.drawable.img_two_c, R.drawable.img_two_c, R.drawable.icesatc,
        R.drawable.icesatc, R.drawable.img_two_c, R.drawable.img_two_c, R.drawable.icesatc,
        R.drawable.icesatc, R.drawable.img_two_c, R.drawable.img_two_c, R.drawable.icesatc)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            index = it.getInt(Index_Param)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery_display, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //index = args.index
        setUpViews()
    }

    private fun setUpViews() {
        textViewTitle.text = titles[index]
        imageViewDisplay.setImageResource(images[index])
        textViewDescription.text = descriptions[index]
        textViewProgress.text = "${index + 1}/${titles.size}"
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            GalleryDisplay().apply {
                arguments = Bundle().apply {
                    putInt(Index_Param, param1)
                }
            }


    }
}
