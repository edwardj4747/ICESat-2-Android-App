package gov.nasa.gsfc.icesat2.icesat_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gov.nasa.gsfc.icesat2.icesat_2.favoritesdb.FavoritesEntry

class FavoritesAdapter(private val allFavorites: List<FavoritesEntry>) : RecyclerView.Adapter<FavoritesAdapter.FavoritesHolder>() {

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
        holder.textViewLatLng.text =  "${favorite.lat}${0x00B0.toChar()}N ${favorite.lng}${0x00B0.toChar()}E"
    }

    override fun getItemCount(): Int {
        return allFavorites.size
    }
}