package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.round

class ImageClassifierHelper(
    private val context: Context
) {

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private lateinit var interpreter: Interpreter


    private val labels = mapOf(
        -1 to "No Cancer",
        1 to "Cancer"
    )

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        interpreter = Interpreter(loadModelFile())
    }

    private fun loadModelFile(): MappedByteBuffer {
        val modelFileName = "cancer_classification.tflite"
        val assetFileDescriptor = context.assets.openFd(modelFileName)

        FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength

            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    fun classifyStaticImage(imageUri: Uri): ClassificationResult? {
        return try {
            val bitmap = getBitmapFromUri(imageUri)
            val inputTensor = preprocessImage(bitmap)


            val output = Array(1) { FloatArray(2) }


            interpreter.run(inputTensor, output)


            val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1


            val predictedLabel = labels[maxIndex] ?: "Unknown"
            val confidenceScore = output[0][maxIndex]


            ClassificationResult(predictedLabel, round(confidenceScore * 100) / 100)
        } catch (e: Exception) {
            showToast("Error: ${e.message}")
            null
        }
    }


    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)


        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(java.nio.ByteOrder.nativeOrder())


        for (y in 0 until resizedBitmap.height) {
            for (x in 0 until resizedBitmap.width) {
                val pixelValue = resizedBitmap.getPixel(x, y)


                val r = (pixelValue shr 16 and 0xFF) / 255.0f
                val g = (pixelValue shr 8 and 0xFF) / 255.0f
                val b = (pixelValue and 0xFF) / 255.0f


                byteBuffer.putFloat(r * 2 - 1)
                byteBuffer.putFloat(g * 2 - 1)
                byteBuffer.putFloat(b * 2 - 1)
            }
        }

        return byteBuffer
    }


    data class ClassificationResult(val label: String, val confidenceScore: Float)
}
