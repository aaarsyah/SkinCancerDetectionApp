package com.dicoding.asclepius.view

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isCancer = intent.getStringExtra("isCancer") ?: "Unknown"
        val confidenceScore = intent.getFloatExtra("confidenceScore", 0.0f)
        val imageUriString = intent.getStringExtra("imageUri")

        displayResults(isCancer, confidenceScore, imageUriString)
    }

    private fun displayResults(isCancer: String, confidenceScore: Float, imageUriString: String?) {

        imageUriString?.let {
            val imageUri = Uri.parse(it)
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.resultImage.setImageBitmap(bitmap)
        }


        val resultText = if (isCancer == "Cancer") {
            "Predicted: Cancer Detected\nConfidence Score: ${confidenceScore * 100}%"
        } else {
            "Predicted: No Cancer Detected\nConfidence Score: ${confidenceScore * 100}%"
        }

        binding.resultText.text = resultText
    }
}
