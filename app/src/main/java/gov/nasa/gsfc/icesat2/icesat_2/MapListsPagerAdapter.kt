package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


private val TAB_TITLES = arrayOf(
    R.string.map,
    R.string.list
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class MapListsPagerAdapter(private val context: Context, fm: FragmentManager, private val listener: IFavoritesFragmentCallback)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return if (position == 0) {
            MapFragment()
        } else {
            val listFragment = ListFragment()
            listFragment.setUpListener(listener)
            listFragment
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }


    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}