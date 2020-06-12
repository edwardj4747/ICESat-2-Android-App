package gov.nasa.gsfc.icesat2.icesat_2.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.fragment.navArgs
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_gallery_container.*


class GalleryPagerAdapter(fm: FragmentManager)
    : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        return GalleryDisplay.newInstance(position)
    }

    override fun getCount(): Int {
        return 16
    }
}


/**
 * Recive the index passed in from [GalleryFragment] through args variable and use that to tell the
 * viewPager which index to display
 */
class GalleryContainerFragment : Fragment() {

    private val args by navArgs<GalleryContainerFragmentArgs>()
    private var index = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery_container, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        index = args.index

        val galleryPagerAdapter = GalleryPagerAdapter(childFragmentManager)
        galleryViewPager.adapter = galleryPagerAdapter
        galleryViewPager.setCurrentItem(index, true)
    }

}