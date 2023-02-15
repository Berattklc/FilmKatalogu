package com.beratklc.filmkatalogu

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_film_olustur.*
import java.io.ByteArrayOutputStream


class fragment_film_olustur : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_film_olustur, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Görünüm ile alakalı bir işlem yapılacak ise burda yapılması daha güvenli diye düşünmüştüm
        //Fragmentlarda butona bir fonksiyon atıyor iseniz bunu setonclicklistener ile yapmamız gerekir
        button.setOnClickListener {
            kaydet(it)
        }
        imageView.setOnClickListener{
            gorselSec(it)
        }
        arguments?.let {
            var gelenBilgi = fragment_film_olusturArgs.fromBundle(it).bilgi
            if (gelenBilgi == "menudengeldim"){
                filmIsmiText.setText("")
                filmTarihiText.setText("")
                button.visibility = View.VISIBLE

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                imageView.setImageBitmap(gorselSecmeArkaPlani)
            }else{

                button.visibility = View.INVISIBLE

                val secilenId = fragment_film_olusturArgs.fromBundle(it).id

                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Filmler",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM filmler WHERE id = ?", arrayOf(secilenId.toString()))

                        val filmIsmiIndex = cursor.getColumnIndex("filmismi")
                        val filmTarihiIndex = cursor.getColumnIndex("filmtarihi")
                        val yemekGorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            filmIsmiText.setText(cursor.getString(filmIsmiIndex))
                            filmTarihiText.setText(cursor.getString(filmTarihiIndex))

                            val byteDizisi = cursor.getBlob(yemekGorseli)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()

                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun kaydet(view: View) {
        //SQLite'a kaydetme
        val filmIsmi = filmIsmiText.text.toString()
        val filmTarihi = filmTarihiText.text.toString()

        if(secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

            //Bitmapi veriye çevirmek için
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Filmler",Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS filmler (id INTEGER PRIMARY KEY, filmismi VARCHAR, filmtarihi VARCHAR, gorsel BLOB)")

                    val sqlString = "INSERT INTO filmler (filmismi, filmtarihi, gorsel) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,filmIsmi)
                    statement.bindString(2,filmTarihi)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()

                }

            }catch (e:Exception){
                e.printStackTrace()
            }

            val action = fragment_film_olusturDirections.actionFragmentFilmOlusturToFragmentListe()
            Navigation.findNavController(view).navigate(action)

        }

    }
    fun gorselSec(view: View){
        //ContextCompat Apı uyumluluğu olması
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //İzin yok,izin istemek gerekiyor
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                //izin zaten verilmiş,tekrar istemeden galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)


            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galeriIntent,2)
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Galeriye gidildiğinde ne yapacağını belirtiyoruz
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null ){
            //Telefonda nerede durduğunu almış olduk
            secilenGorsel = data.data
            //neden try catch içine alıyoruz
            //Sonuçta bir uri var biz sadece konumunu aldık konumdan dosyaya çevirmeye çalışırken hataya sebep olabilr
            try {
                context?.let {
                    if (secilenGorsel != null && context != null){
                        if (Build.VERSION.SDK_INT>=28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(secilenBitmap)
                        }else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            imageView.setImageBitmap(secilenBitmap)
                        }

                    }
                }



            }catch (e:Exception){
                e.printStackTrace()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap:Bitmap,maximumBoyut : Int) : Bitmap{

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if (bitmapOrani>1){
            //Görsel yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        }else{
            //Görselimiz dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)

    }

}