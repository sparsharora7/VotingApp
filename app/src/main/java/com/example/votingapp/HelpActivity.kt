package com.example.votingapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    private lateinit var questionInput: EditText
    private lateinit var submitButton: Button
    private lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // Initialize UI elements
        questionInput = findViewById(R.id.questionInput)
        submitButton = findViewById(R.id.submitButton)
        responseTextView = findViewById(R.id.responseTextView)

        // Set up the button click listener
        submitButton.setOnClickListener {
            val question = questionInput.text.toString()
            if (question.isNotEmpty()) {
                getResponse(question)
            }
        }
    }

    private fun getResponse(question: String) {
        // A simple set of predefined responses for demonstration
        val responses = mapOf(
            "What is voting?" to "Voting is the process of making a choice or decision, often in an election.",
            "How do I vote?" to "You can vote by registering and then casting your vote on election day.",
            "What are the voting hours?" to "Voting hours vary by location, so please check with your local election office."
        )

        // Get response from the predefined map or a default response
        val response = responses[question] ?: "I'm sorry, I don't understand that question. Please try again."

        // Update the response TextView
        responseTextView.text = response
    }
}
