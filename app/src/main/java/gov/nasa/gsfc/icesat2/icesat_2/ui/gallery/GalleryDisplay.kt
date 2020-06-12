package gov.nasa.gsfc.icesat2.icesat_2.ui.gallery

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import gov.nasa.gsfc.icesat2.icesat_2.R
import kotlinx.android.synthetic.main.fragment_gallery_display.*
import kotlin.math.abs


/**
 * Interface to provide methods to be executed in GalleryDisplay fragment that can be called from [OnSwipeTouchListener]
 */
interface GalleryDisplayCallback {
    fun nextPhoto()
    fun previousPhoto()
}

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val Index_Param = "param1"


private const val TAG = "GalleryDisplay"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryDisplay.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryDisplay : Fragment(), GalleryDisplayCallback {
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
        //galleryDisplayConstraintLayout.setOnTouchListener(object : OnSwipeTouchListener(context, this) {})
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

    override fun nextPhoto() {
        if (index + 1 < titles.size) {
            index += 1
            Log.d(TAG, "going to next photo from callback; new index is $index")
            setUpViews()
        }
    }

    override fun previousPhoto() {
        if (index > 0) {
            index -= 1
            Log.d(TAG, "going to previous photo from callback. new index is $index")
            setUpViews()
        }
    }
}

open class OnSwipeTouchListener(context: Context?, private val listener: GalleryDisplayCallback) : OnTouchListener {
    val SWIPE_THRESHOLD = 100
    val SWIPE_VELOCITY_THRESHOLD = 100

    private val gestureDetector: GestureDetector
    fun onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft")
        listener.nextPhoto()
    }
    fun onSwipeRight() {
        Log.d(TAG, "onSwipeRight")
        listener.previousPhoto()
    }
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val distanceX = e2.x - e1.x
            val distanceY = e2.y - e1.y
            if (abs(distanceX) > abs(distanceY) && abs(distanceX) > SWIPE_THRESHOLD
                && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0) onSwipeRight() else onSwipeLeft()
                return true
            }
            return false
        }
    }

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}
