package com.example.votingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class VoteConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_confirmation)

        val tvVoteConfirmation: TextView = findViewById(R.id.tvVoteConfirmation)
        val tvVoteID: TextView = findViewById(R.id.tvVoteID)
        val btnGoBack: Button = findViewById(R.id.btnGoBack)

        // Retrieve the vote ID passed from the previous activity (optional)
        val voteId = intent.getStringExtra("VOTE_ID")
        if (voteId != null) {
            tvVoteID.text = "Vote ID: $voteId"
        } else {
            tvVoteID.text = "" // Hide Vote ID if not provided
        }

        // Button to return to home screen
        btnGoBack.setOnClickListener {
            // Go back to HomeActivity or MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close this activity
        }
    }
}
