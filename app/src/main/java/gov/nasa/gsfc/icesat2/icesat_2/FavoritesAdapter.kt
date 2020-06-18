package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.util.Log
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
        //holder.textViewLatLng.text =  "Greenbelt, MD; ${favorite.lat}${0x00B0.toChar()}N ${favorite.lng}${0x00B0.toChar()}E"
        val locString = Geocoding.getAddress(context, favorite.lat, favorite.lng)
        val locArray = Geocoding.getAdminCountry(context, favorite.lat, favorite.lng)
        Log.d(TAG, "locArray: ${locArray[0]} ${locArray[1]}")

        var locationString = "Unknown Location"
        if (locArray[0] == null && locArray[1] != null) {
            Log.d(TAG, "for ${locArray[0]} ${locArray[1]} location string is ${locArray[1]}")
            locationString = locArray[1]!!
        } else if (locArray[0] != null && locArray[1] != null) {
            locationString = "${locArray[0]}, ${locArray[1]}"
        }

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