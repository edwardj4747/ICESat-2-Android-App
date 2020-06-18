package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry

private const val TAG = "FavoritesAdapter"

class FavoritesAdapter(private val context: Context, private val allFavorites: List<FavoritesEntry>) : RecyclerView.Adapter<FavoritesAdapter.FavoritesHolder>() {

    inner class FavoritesHolder(private val view: View) : RecyclerView.ViewHolder(view){
        var textViewDateTime: TextView = view.findViewById(R.id.textViewDateTime)
        var textViewLatLng: TextView = view.findViewById(R.id.textViewLatLng)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_list_ticket, parent, false)
        return FavoritesHolder(view)
    }


    override fun onBindViewHolder(holder: FavoritesHolder, position: Int) {
        val favorite = allFavorites[position]
        holder.textViewDateTime.text = favorite.dateString

        val locationString = Geocoding.getGeographicInfo(Geocoder(context), favorite.lat, favorite.lng)
        holder.textViewLatLng.text = context.getString(R.string.geoLatLng, locationString,
            favorite.lat.toString(), 0x00B0.toChar(), favorite.lng.toString(), 0x00B0.toChar())
    }

    override fun getItemCount(): Int {
        return allFavorites.size
    }

    fun getFavoriteAt(index: Int): FavoritesEntry {
        return allFavorites[index]
    }
}