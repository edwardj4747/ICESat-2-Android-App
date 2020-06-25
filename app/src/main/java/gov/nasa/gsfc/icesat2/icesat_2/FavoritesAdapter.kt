package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry


private const val TAG = "FavoritesAdapter"

class FavoritesAdapter(
    private val context: Context,
    private val allFavorites: List<FavoritesEntry>
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesHolder>() {

    private val geocoder = Geocoder(context)
    private val displayLocations = allFavorites.size < 10

    private lateinit var listener: ILaunchSingleMarkerMap

    inner class FavoritesHolder(private val view: View) : RecyclerView.ViewHolder(view){
        var textViewDateTime: TextView = view.findViewById(R.id.textViewDateTime)
        var textViewLatLng: TextView = view.findViewById(R.id.textViewLatLng)
        var locationListLinearLayout: LinearLayout = view.findViewById(R.id.locationListLinearLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_list_ticket, parent, false)
        return FavoritesHolder(view)
    }


    override fun onBindViewHolder(holder: FavoritesHolder, position: Int) {
        val favorite = allFavorites[position]
        holder.textViewDateTime.text = favorite.dateString

        holder.textViewLatLng.text = context.getString(
            R.string.geoLatLng, favorite.geocodedLocation,
            favorite.lat.toString(), 0x00B0.toChar(), favorite.lng.toString(), 0x00B0.toChar())

        holder.locationListLinearLayout.setOnClickListener {

            val selectedFavorite = allFavorites[holder.adapterPosition]
            Log.d(TAG, "onClick at Position ${holder.adapterPosition}")
            Log.d(TAG, "data of point is ${allFavorites[holder.adapterPosition].dateString}")

            //callback inside of [FavoritesFragment] to launch going to the singleMarkerMap
            listener.navigateToSingleMarkerMap(selectedFavorite.lat, selectedFavorite.lng, selectedFavorite.dateString, selectedFavorite.dateObjectTime)
        }


    }

    override fun getItemCount(): Int {
        return allFavorites.size
    }

    fun setListener(listener: ILaunchSingleMarkerMap) {
        this.listener = listener
    }

    fun getFavoriteAt(index: Int): FavoritesEntry {
        return allFavorites[index]
    }
}