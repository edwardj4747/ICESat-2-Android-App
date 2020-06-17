package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "ListRecyclerViewAdapter"

//ViewHolder - storing references to the views involved to make this more efficient
class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var textViewDateTime: TextView = view.findViewById(R.id.textViewDateTime)
    var textViewLatLng: TextView = view.findViewById(R.id.textViewLatLng)
    var imageView: ImageView = view.findViewById(R.id.imageView)
}

class ListRecyclerViewAdapter(val context: Context, private val allPoints: ArrayList<Point>) : RecyclerView.Adapter<ListViewHolder>(){


    var headerLocations = arrayOf(4, 10)
    var headerIndex = 0
    var offset = 0

    private val headerPadding = 30
    private val scale: Float = context.resources.displayMetrics.density
    private val dpAsPixels = (headerPadding * scale + 0.5f).toInt()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_list_ticket, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {


            if (position == 4 || position == 10) {
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