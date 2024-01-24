package com.example.myapplication

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val (bitmap,uri)= getImageFrmIntent()
        setImage(uri,bitmap)

        binding.btnBack.setOnClickListener{
            onBackPressed()
        }
    }

    private fun setImage(uri:String?,bitmap:Bitmap?){
        binding.apply {
            imgView.setImageDrawable(null)
            if(uri != null){
                imgView.setImageURI(Uri.parse(uri))
            }else{
                imgView.setImageBitmap(bitmap)
            }
        }

    }


    private fun getImageFrmIntent(): Pair<Bitmap?, String?> {
        val intent = getIntent()
        val bitmap = if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra("bitmapimage", Bitmap::class.java)
        else intent.getParcelableExtra("bitmapimage")

        val uri = intent.getStringExtra("uriimage")
        return Pair(bitmap,uri)


    }



}