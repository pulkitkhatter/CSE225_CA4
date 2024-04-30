package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class AllRequestStatus : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_request_status)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        loadMeetings()
    }

    private fun loadMeetings() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val meetingsRef = database.child("meetings")

            // Query meetings where status is either accepted or rejected
            meetingsRef.orderByChild("status")
                .startAt("accepted")
                .endAt("rejected")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Process the retrieved meetings
                        val meetings = mutableListOf<Meeting>()
                        for (meetingSnapshot in snapshot.children) {
                            val meeting = meetingSnapshot.getValue(Meeting::class.java)
                            meeting?.let {
                                meetings.add(it)
                            }
                        }
                        // Now you have a list of meetings with status accepted or rejected
                        // Do whatever you need with the meetings list
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                        Toast.makeText(this@AllRequestStatus, "Failed to load meetings: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
