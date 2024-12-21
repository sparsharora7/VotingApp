package com.example.votingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutionException

class HomeActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var btnVote: Button
    private var imageCapture: ImageCapture? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        btnVote = findViewById(R.id.btnVote)

        // Disable the vote button initially
        btnVote.isEnabled = false

        startCamera()

        btnCapture.setOnClickListener { captureFace() }
        btnVote.setOnClickListener { castVote() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider) // Set the surface provider for the preview
            }

        imageCapture = ImageCapture.Builder().build()

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind the camera to the lifecycle, along with preview and image capture
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (exc: Exception) {
            Log.e("HomeActivity", "Use case binding failed", exc)
        }
    }

    private fun captureFace() {
        val executor = ContextCompat.getMainExecutor(this)
        imageCapture?.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        detectFace(inputImage)
                    }
                    imageProxy.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Face Capture", "Image capture failed", exception)
                }
            })
    }

    private fun detectFace(image: InputImage) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    if (faces.size == 1) {
                        val face = faces[0]
                        saveFaceData(face)
                    } else {
                        Toast.makeText(this, "Multiple faces detected. Only one person can register!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No face detected!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Face Detection", "Failed to detect face", e)
            }
    }

    private fun saveFaceData(face: Face) {
        val userId = mAuth.currentUser?.uid ?: return

        val faceData = hashMapOf(
            "boundingBox" to face.boundingBox.flattenToString(),
            "leftEyeOpenProbability" to face.leftEyeOpenProbability,
            "rightEyeOpenProbability" to face.rightEyeOpenProbability
        )

        db.collection("users").document(userId).set(faceData)
            .addOnSuccessListener {
                Toast.makeText(this, "Face Registered Successfully", Toast.LENGTH_SHORT).show()
                // Enable the vote button after successful face registration
                btnVote.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register face", Toast.LENGTH_SHORT).show()
            }
    }

    private fun castVote() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val voteData = hashMapOf(
                "voted" to true,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("votes").document(currentUser.uid).set(voteData)
                .addOnSuccessListener {
                    // Show success message
                    Toast.makeText(this, "Vote Cast Successfully", Toast.LENGTH_SHORT).show()

                    // After successful vote, navigate to VoteConfirmationActivity
                    navigateToVoteConfirmation()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to cast vote", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    // Navigate to VoteConfirmationActivity after voting
    private fun navigateToVoteConfirmation() {
        val intent = Intent(this, VoteConfirmationActivity::class.java)
        startActivity(intent)
        finish() // Ensure HomeActivity is removed from the back stack
    }
}
