package com.beratklc.filmkatalogu

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_liste.*


class fragment_liste : Fragment() {

    var filmIsmiListesi = ArrayList<String>()
    var filmIdListesi = ArrayList<Int>()
    private lateinit var listeAdapter : ListeRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liste, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeAdapter = ListeRecyclerAdapter(filmIsmiListesi,filmIdListesi)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listeAdapter

        sqlVeriAlma()
    }

    fun sqlVeriAlma(){

        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Filmler",Context.MODE_PRIVATE,null)

                val cursor = database.rawQuery("SELECT * FROM filmler",null)
                val filmIsmiIndex = cursor.getColumnIndex("filmismi")
                val filmIdIndex = cursor.getColumnIndex("id")

                filmIsmiListesi.clear()
                filmIdListesi.clear()

                while (cursor.moveToNext()){
                    filmIsmiListesi.add(cursor.getString(filmIsmiIndex))
                    filmIdListesi.add(cursor.getInt(filmIdIndex))
                }
                //Eğer veriler değişiyor ise listeye söylicek recyclerview güncellencek
                listeAdapter.notifyDataSetChanged()
                cursor.close()

            }

        }catch (e:Exception){

        }



    }


}