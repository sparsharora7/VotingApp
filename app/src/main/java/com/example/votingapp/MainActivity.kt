package com.example.votingapp

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var tts: TextToSpeech
    private var isTtsEnabled = false

    // Define UI elements
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInButton: Button
    private lateinit var btnToggleTTS: Button // TTS button
    private lateinit var howCanIHelpYouButton: ImageButton // Help button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        signUpButton = findViewById(R.id.signUpButton)
        signInButton = findViewById(R.id.signInButton)
        btnToggleTTS = findViewById(R.id.btnToggleTTS) // Initialize TTS button
        howCanIHelpYouButton = findViewById(R.id.howCanIHelpYouButton) // Initialize Help button

        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US // Set language to US English
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }

        // Set up button click listeners
        signUpButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            signUp(email, password)
        }

        signInButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            signIn(email, password)
        }

        // Set up TTS button click listener
        btnToggleTTS.setOnClickListener {
            isTtsEnabled = !isTtsEnabled
            if (isTtsEnabled) {
                Toast.makeText(this, "TTS Enabled", Toast.LENGTH_SHORT).show()
                speak("Text to Speech is now enabled.")
            } else {
                Toast.makeText(this, "TTS Disabled", Toast.LENGTH_SHORT).show()
                speak("Text to Speech is now disabled.")
            }
        }

        // Set up Help button click listener
        howCanIHelpYouButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java) // Create an Intent to start HelpActivity
            startActivity(intent)
        }
    }

    // Sign Up Method with password validation
    private fun signUp(email: String, password: String) {
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return // Stop the sign-up process if the password is invalid
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Sign In Method with password validation
    private fun signIn(email: String, password: String) {
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return // Stop the sign-in process if the password is invalid
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign In Successful", Toast.LENGTH_SHORT).show()

                    // Navigate to HomeActivity after successful sign-in
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Optional: call finish() to prevent going back to the sign-in screen
                } else {
                    Toast.makeText(this, "Sign In Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun speak(text: String) {
        if (isTtsEnabled) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        // Shutdown TTS when activity is destroyed
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
