package com.example.whatsthat

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.TextView

import java.io.IOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.services.vision.v1.model.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream

class CloudVision(val textView: TextView?, val context: Context) {
    private val CLOUD_VISION_API_KEY = BuildConfig.CLOUD_VISION_API_KEY
    private val TAG = MainActivity::class.java.simpleName

    fun makeApiCall(bitmap: Bitmap) {
        val annotateImageRequests = ArrayList<AnnotateImageRequest>()

        val annotateImageReq = AnnotateImageRequest()
        annotateImageReq.image = getImageEncodeImage(bitmap)
        annotateImageRequests.add(annotateImageReq)

        annotateImageReq.features = object : java.util.ArrayList<Feature>() {
            init {
                val labelDetection = Feature()
                labelDetection.type = "LABEL_DETECTION"
                    labelDetection.maxResults = 10
                add(labelDetection)
            }
        }

        doAsync {
            try {
                val httpTransport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = GsonFactory.getDefaultInstance()

                val requestInitializer = VisionRequestInitializer(CLOUD_VISION_API_KEY)

                val builder = Vision.Builder(httpTransport, jsonFactory, null)
                builder.setVisionRequestInitializer(requestInitializer)

                val vision = builder.build()

                val batchAnnotateImagesRequest = BatchAnnotateImagesRequest()
                batchAnnotateImagesRequest.requests = annotateImageRequests

                val annotateRequest = vision.images().annotate(batchAnnotateImagesRequest)
                annotateRequest.disableGZipContent = true
                val response = annotateRequest.execute()
                val responseString = convertResponseToString(response)
                uiThread {
                    if (isAHotDog(responseString)) {
                        textView?.text = context.getResources().getString(R.string.is_a_hotdog)
                        textView?.textSize = 36F
                        textView?.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        textView?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen))
                    } else {
                        textView?.text = context.getResources().getString(R.string.is_not_a_hotdog)
                        textView?.textSize = 36F
                        textView?.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        textView?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed))
                    }
                }
            } catch (e: GoogleJsonResponseException) {
                Log.d(TAG, "failed to make API request because " + e.content)
            } catch (e: IOException) {
                Log.d(TAG, "failed to make API request because of other IOException " + e.message)
            }
        }
    }

    private fun isAHotDog(resultsString: String): Boolean {
        println(resultsString)
        if(resultsString.contains("Hot dog")) return true
        return false
    }

    private fun getImageEncodeImage(bitmap: Bitmap): Image {
        val base64EncodedImage = Image()
        // Convert the bitmap to a JPEG
        // Just in case it's a format that Android understands but Cloud Vision
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        // Base64 encode the JPEG
        base64EncodedImage.encodeContent(imageBytes)
        return base64EncodedImage
    }

    private fun convertResponseToString(response: BatchAnnotateImagesResponse): String {

        val imageResponses = response.responses[0]
        return formatAnnotation(imageResponses.labelAnnotations)
    }

    private fun formatAnnotation(entityAnnotation: List<EntityAnnotation>?): String {
        var message = ""

        if (entityAnnotation != null) {
            for (entity in entityAnnotation) {
                message = message + "    " + entity.description + " " + entity.score
                message += "\n"
            }
        } else {
            message = "Nothing Found"
        }
        return message
    }
}