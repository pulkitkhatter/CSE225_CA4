package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MeetingRequest : AppCompatActivity() {
    private lateinit var meetingAdapter: MeetingAdapter
    private lateinit var meetingsList: MutableList<Meeting>
    private lateinit var recyclerViewMeetings: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_request)
        val buttonSeeAllMeetings = findViewById<Button>(R.id.buttonSeeAllMeetings)
        buttonSeeAllMeetings.setOnClickListener {
            // Create an Intent to navigate to the other activity
            val intent = Intent(this, AllRequestStatus::class.java)
            startActivity(intent)
        }
        // Initialize RecyclerView and adapter
        meetingsList = mutableListOf()
        meetingAdapter = MeetingAdapter(meetingsList, object : MeetingAdapter.MeetingClickListener {
            override fun onAcceptClick(meeting: Meeting) {
                updateMeetingStatus(meeting, "Accepted")
            }


            override fun onRejectClick(meeting: Meeting) {
                updateMeetingStatus(meeting, "Rejected")
            }

        })
        recyclerViewMeetings = findViewById(R.id.recyclerViewMeetings)
        recyclerViewMeetings.layoutManager = LinearLayoutManager(this)
        recyclerViewMeetings.adapter = meetingAdapter

        // Load pending meetings data from Firebase Firestore
        loadPendingMeetings()
    }

    private fun loadPendingMeetings() {
        val db = FirebaseFirestore.getInstance()
        db.collection("meetings")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                meetingsList.clear()
                for (document in documents) {
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val timestamp = document.getTimestamp("date")
                    val date = timestamp?.toDate()?.formatToString() ?: ""
                    val id = document.id
                    val status = document.getString("status") ?: ""
                    val meeting = Meeting(id, title, description, date, status)
                    meetingsList.add(meeting)
                }
                meetingAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    // Extension function to format Date to String
    private fun Date.formatToString(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(this)
    }
    private fun updateMeetingStatus(meeting: Meeting, status: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("meetings")
            .document(meeting.id)
            .update("status", status)
            .addOnSuccessListener {
                // Update successful
                // Reload the meetings after updating status
                loadPendingMeetings()
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }
    
}
