package com.example.whatsthat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val GALLERY_IMAGE_REQUEST = 1
    private val CAMERA_IMAGE_REQUEST = 2
    private val TAG = MainActivity::class.java.simpleName
    private val FILE_NAME = "temp.jpg"

    private var mobileMainImage: ImageView? = null
    private var mobileMainImageDetails: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder
                .setMessage(R.string.dialog_select_prompt)
                //CAMERA
                .setNegativeButton(R.string.dialog_select_camera) { _, _ ->
                    startCamera()
                }
                //GALLERY
                .setPositiveButton(R.string.dialog_select_gallery) { _, _ ->
                    startImageGallery()
                }
            builder.create().show()
        }

        mobileMainImage = findViewById(R.id.mainImage)
        mobileMainImageDetails = findViewById(R.id.mainImageDetails)
    }

    private fun startImageGallery() {
        val imageGallery = Intent()
        imageGallery.type = "image/*"
        imageGallery.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                imageGallery,
                "Select Picture"
            ), GALLERY_IMAGE_REQUEST
        )
    }

    private fun startCamera() {
        val imageCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoUri =
            FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", getCameraFile())
        imageCapture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(imageCapture, CAMERA_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            uploadImage(data.data)
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val photoUri: Uri =
                FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", getCameraFile())
            uploadImage(photoUri)
        }
    }

    private fun getCameraFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    private fun uploadImage(uri: Uri?) {
        if (uri != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                mobileMainImageDetails?.setText(R.string.loading_message)
                mobileMainImageDetails?.setTextColor(ContextCompat.getColor(this, R.color.colorGrey))
                mobileMainImageDetails?.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
                mobileMainImageDetails?.textSize = 18F

                mobileMainImage?.setImageBitmap(bitmap)
                val cloudVision = CloudVision(mobileMainImageDetails, this)
                cloudVision.makeApiCall(bitmap)

            } catch (e: IOException) {
                Log.d(TAG, "Image picking failed because " + e.message)
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
            }

        } else {
            Log.d(TAG, "Image picker gave us a null image.")
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
