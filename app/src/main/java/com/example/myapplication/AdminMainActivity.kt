package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        // Find the joinButton by its ID
        val joinButton = findViewById<Button>(R.id.joinButton)

        // Set OnClickListener for the joinButton
        joinButton.setOnClickListener {
            // Log click event
            Log.d("AdminMainActivity", "Join button clicked")
            // Open the Google Meet website
            openGoogleMeetWebsite()
        }
    }

    private fun openGoogleMeetWebsite() {
        // URL of the Google Meet website
        val websiteUrl = "https://meet.google.com/vnd-oskj-wwc"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
        startActivity(intent)
    }
}
