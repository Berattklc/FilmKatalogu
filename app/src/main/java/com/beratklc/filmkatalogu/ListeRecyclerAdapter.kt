package com.beratklc.filmkatalogu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListeRecyclerAdapter(val filmListesi:ArrayList<String>,val idListesi : ArrayList<Int>) : RecyclerView.Adapter<ListeRecyclerAdapter.FilmHolder>() {
    class  FilmHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)
        return FilmHolder(view)
    }

    override fun onBindViewHolder(holder: FilmHolder, position: Int) {
        holder.itemView.recycler_row_text.text = filmListesi[position]
        holder.itemView.setOnClickListener {
            val action = fragment_listeDirections.actionFragmentListeToFragmentFilmOlustur("recyclerdangeldim",idListesi[position])
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return filmListesi.size
    }
}