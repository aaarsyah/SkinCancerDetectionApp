package com.dicoding.asclepius.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.dicoding.asclepius.databinding.FragmentPredictBinding
import com.dicoding.asclepius.entity.PredictionHistory
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.room.AppDatabase
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PredictFragment : Fragment() {
    private var _binding: FragmentPredictBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var getImage: ActivityResultLauncher<Intent>


    private val predictViewModel: PredictViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageClassifierHelper = ImageClassifierHelper(requireContext())

        getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    startCrop(selectedImageUri)
                }
            }
        }


        predictViewModel.currentImageUri.observe(viewLifecycleOwner) { uri ->
            showImage(uri)
        }

        binding.galleryButton.setOnClickListener {
            if (checkPermission()) {
                startGallery()
            } else {
                requestPermission()
            }
        }

        binding.analyzeButton.setOnClickListener {
            analyzeImage()
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .start(requireActivity(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                val resultUri = UCrop.getOutput(data!!)
                if (resultUri != null) {
                    predictViewModel.setCurrentImageUri(resultUri)
                    showToast("Image cropped successfully")
                } else {
                    showToast("Crop result is null")
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showToast(cropError?.message ?: "Crop error")
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImage.launch(intent)
    }

    private fun showImage(imageUri: Uri?) {
        binding.previewImageView.setImageURI(null)
        if (imageUri != null) {
            binding.previewImageView.setImageURI(imageUri)
        } else {
            showToast("No image selected")
        }
    }

    private fun analyzeImage() {
        val currentImageUri = predictViewModel.currentImageUri.value
        if (currentImageUri != null) {
            val result = imageClassifierHelper.classifyStaticImage(currentImageUri)
            if (result != null) {
                moveToResult(result)
                savePredictionHistory(currentImageUri.toString(), result.label, result.confidenceScore)
            } else {
                showToast("Failed to classify the image.")
            }
        } else {
            showToast("Please select an image first")
        }
    }


    private fun savePredictionHistory(imageUri: String, label: String, confidenceScore: Float) {
        val predictionHistory = PredictionHistory(imageUri = imageUri, label = label, confidenceScore = confidenceScore)

        val database = AppDatabase.getDatabase(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            database.predictionHistoryDao().insert(predictionHistory)
        }
    }

    private fun moveToResult(result: ImageClassifierHelper.ClassificationResult) {
        val intent = Intent(requireActivity(), ResultActivity::class.java).apply {
            putExtra("isCancer", result.label)
            putExtra("confidenceScore", result.confidenceScore)
            putExtra("imageUri", predictViewModel.currentImageUri.value.toString())
        }
        startActivity(intent)
    }

    private fun checkPermission(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGallery()
            } else {
                showToast("Permission denied to read your external storage")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}
