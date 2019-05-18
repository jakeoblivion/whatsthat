package com.example.whatsthat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.support.v7.app.AlertDialog


class MainActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1
    private val CAMERA_IMAGE_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val imageCapture = Intent("android.media.action.IMAGE_CAPTURE")

        val imageGallery = Intent()
        imageGallery.type = "image/*"
        imageGallery.action = Intent.ACTION_GET_CONTENT

        fab.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder
                .setMessage(R.string.dialog_select_prompt)
                //CAMERA
                .setNegativeButton(R.string.dialog_select_camera) { _, _ ->
                    startActivityForResult(imageCapture, CAMERA_IMAGE_REQUEST)
                }
                //GALLERY
                .setPositiveButton(R.string.dialog_select_gallery) { _, _ ->
                    startActivityForResult(
                        Intent.createChooser(
                            imageGallery,
                            "Select Picture"
                        ), PICK_IMAGE
                    )
                }
            builder.create().show()
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
