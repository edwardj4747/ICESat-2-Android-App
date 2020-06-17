package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "ListRecyclerViewAdapter"

//ViewHolder - storing references to the views involved to make this more efficient
class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var textViewDateTime: TextView = view.findViewById(R.id.textViewDateTime)
    var textViewLatLng: TextView = view.findViewById(R.id.textViewLatLng)
    var imageView: ImageView = view.findViewById(R.id.imageView)
}

class ListRecyclerViewAdapter(val context: Context, private val allPoints: ArrayList<Point>) : RecyclerView.Adapter<ListViewHolder>(){

   /* companion object {
        fun receiveHeaderLocations(headerLocations: ArrayList<Int>) {
            Log.d(TAG, "receive header Locations \n $headerLocations")
        }
    }

    private var headerLocations = MapFragment.getStartOfChains()*/

    private val headerPadding = 30
    private val scale: Float = context.resources.displayMetrics.density
    private val dpAsPixels = (headerPadding * scale + 0.5f).toInt()

    private var headerLocations = ArrayList<Int>()

    init {
        headerLocations = calculateHeaderLocations(allPoints)
        Log.d(TAG, "init, headerLocations calculated as $headerLocations")
    }

    private fun calculateHeaderLocations(chain: ArrayList<Point>) : ArrayList<Int> {
        val arr = ArrayList<Int>()
        arr.add(0)
        for (i in 1 until allPoints.size) {
            if (!onSameChain(chain[i - 1], chain[i])) {
                arr.add(i + arr.size)
            }
        }
        return arr
    }

    private fun onSameChain(p1: Point, p2: Point) : Boolean {
        val timingThreshold = 60
        return p1.dateObject.time + timingThreshold * 1000 > p2.dateObject.time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_list_ticket, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        if (headerLocations.contains(position)) {
                holder.textViewDateTime.text = "HEADER!!"
                holder.textViewDateTime.typeface = Typeface.DEFAULT_BOLD
                holder.textViewDateTime.setPadding(dpAsPixels, dpAsPixels / 2, dpAsPixels, dpAsPixels / 10)
                holder.textViewLatLng.visibility = View.GONE
                holder.imageView.visibility = View.GONE
            } else {
                val item = allPoints[position - determineOffset(position)]
                holder.textViewDateTime.text = item.dateString
                holder.textViewDateTime.typeface = Typeface.DEFAULT
                holder.textViewDateTime.setPadding(0, 0, 0, 0)
                holder.textViewLatLng.visibility = View.VISIBLE
                holder.imageView.visibility = View.VISIBLE

                holder.textViewLatLng.text = "${item.latitude}${0x00B0.toChar()}N ${item.longitude}${0x00B0.toChar()}E"
            }


    }

    override fun getItemCount(): Int {
       return allPoints.size + headerLocations.size
    }

    private fun determineOffset(position: Int): Int {
        var index = 0
        while (index < headerLocations.size && position > headerLocations[index]) {
            index++
        }
        return index
    }

}