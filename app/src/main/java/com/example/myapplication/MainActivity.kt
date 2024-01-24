package com.example.myapplication

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
   private val viewModel:MainViewModel by viewModels()

    private var detectImage : Bitmap ?= null
    private var detectUri : Uri ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.resultText.movementMethod=ScrollingMovementMethod()
        binding.btnCopy.bringToFront()
        copy()
        buttonClick()
    }


    private fun buttonClick(){
        binding.apply {
            btnCamera.setOnClickListener{
                CameraPermission()
            }

            btnGallery.setOnClickListener{
                GalleryPermission()
            }




            btnShowImage.setOnClickListener{
                if(detectImage!=null && detectUri !=null){
                    val intent = Intent(this@MainActivity,ResultActivity::class.java)
                    intent.putExtra("uriimage",detectUri.toString())
                    startActivity(intent)
                }else if(detectImage!=null){
                    val intent = Intent(this@MainActivity,ResultActivity::class.java)
                    intent.putExtra("bitmapimage",detectImage)
                    startActivity(intent)
                }else{
                    Toast.makeText(this@MainActivity,"No Image selected",Toast.LENGTH_SHORT).show()
                }
            }



        }
    }


    //Camera

    private fun CameraPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION)
        }else{
            openCamera()
        }
    }

    private fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

        private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                detectImage = result.data?.extras?.get("data") as Bitmap
                setRecognizitionTextFromBitmap(detectImage!!)
            }
        }




    // Gallery


    private fun GalleryPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                GALLERY_PERMISSION)
        }else{
            openGallery()
        }
    }





    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        val uri:Uri?=result.data?.data
        if(result.resultCode == RESULT_OK && uri != null){
            detectUri = uri
            val inputStream = contentResolver.openInputStream(detectUri!!)
            detectImage =BitmapFactory.decodeStream(inputStream)
            setRecognizitionTextFromBitmap(detectImage!!)

        }

    }





//Text Recognition
    private fun setRecognizitionTextFromBitmap(bitmap: Bitmap){
        runBlocking {
            withContext(Dispatchers.Default){
                viewModel.textRecognizer(this@MainActivity,binding.resultText,bitmap)
            }
        }
    }









    private fun copy(){
        if(!binding.resultText.text.isNullOrEmpty()){
            binding.btnCopy.setOnClickListener{
                val clipboard:ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("copied",binding.resultText.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this,"Text Copied",Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){

            CAMERA_PERMISSION->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera()
                }else{
                    Toast.makeText(this,"Camera Permission Denied",Toast.LENGTH_SHORT).show()
                }
            }


            GALLERY_PERMISSION->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openGallery()
                }else{
                    Toast.makeText(this,"Gallery Permission Denied",Toast.LENGTH_SHORT).show()
                }
            }


        }


    }






    companion object{
        private const val CAMERA_PERMISSION:Int = 0
        private const val GALLERY_PERMISSION:Int = 1
    }



}