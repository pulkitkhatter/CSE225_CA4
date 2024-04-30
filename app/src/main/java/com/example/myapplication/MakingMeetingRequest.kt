package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MakingMeetingRequest : AppCompatActivity() {

    private lateinit var datePickerButton: Button
    private lateinit var selectedDateTextView: TextView
    private lateinit var userSpinner: Spinner
    private lateinit var slotsSpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_making_meeting_request)
        datePickerButton = findViewById(R.id.dateButton)
        selectedDateTextView = findViewById(R.id.selectedDateTextView)
        selectedDateTextView.text = "No date selected"
        userSpinner = findViewById(R.id.userSpinner)
        slotsSpinner = findViewById(R.id.slotsSpinner)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        submitButton = findViewById(R.id.submitButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        datePickerButton.setOnClickListener {
            showDatePicker()
        }


        val users = arrayOf("Deepak", "Naresh", "Pulkit", "ketan")
        val userAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, users)
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userSpinner.adapter = userAdapter

        val emptySlots = arrayOf<String>()
        val slotsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, emptySlots)
        slotsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        slotsSpinner.adapter = slotsAdapter

        userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUser = users[position]
                val userSlots = getUserSlots(selectedUser)
                updateSlotsSpinner(userSlots)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        submitButton.setOnClickListener {
            if (validateInputs()) {
                // Proceed with submission
                submitMeetingRequest()
            } else {
                Toast.makeText(this, "Please fill in all details.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = formatDate(year, monthOfYear, dayOfMonth)
                selectedDateTextView.text = selectedDate
            },
            year,
            month,
            day
        )

        // Set OnDismissListener to handle case when no date is selected
        datePickerDialog.setOnDismissListener {
            if (selectedDateTextView.text.isEmpty()) {
                selectedDateTextView.text = "No date selected"
            }
        }

        datePickerDialog.show()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        return dateFormat.format(cal.time)
    }

    private fun getUserSlots(user: String): Array<String> {
        return when (user) {
            "Deepak" -> arrayOf("12:00 PM - 1:00 PM", "1:00 PM - 2:00 PM", "2:00 PM - 3:00 PM")
            "Naresh" -> arrayOf("3:00 PM - 4:00 PM", "4:00 PM - 5:00 PM", "5:00 PM - 6:00 PM")
            "Pulkit" -> arrayOf("9:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM")
            "ketan" -> arrayOf("6:00 PM - 7:00 PM", "7:00 PM - 8:00 PM", "8:00 PM - 9:00 PM")
            else -> emptyArray()
        }
    }


    private fun updateSlotsSpinner(slots: Array<String>) {
        val slotsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, slots)
        slotsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        slotsSpinner.adapter = slotsAdapter
    }

    private fun validateInputs(): Boolean {
        val title = findViewById<EditText>(R.id.titleEditText).text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val selectedDate = selectedDateTextView.text.toString().trim()
        val selectedUser = userSpinner.selectedItem.toString()
        val selectedSlot = slotsSpinner.selectedItem.toString()

        return title.isNotEmpty() && description.isNotEmpty() && selectedDate.isNotEmpty() && selectedUser.isNotEmpty() && selectedSlot.isNotEmpty()
    }
    private fun submitMeetingRequest() {
        val title = findViewById<EditText>(R.id.titleEditText).text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val selectedDate = selectedDateTextView.text.toString().trim()
        val selectedUser = userSpinner.selectedItem.toString()
        val selectedSlot = slotsSpinner.selectedItem.toString()

        val db = FirebaseFirestore.getInstance()

        // Convert selected date to timestamp
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val date = dateFormat.parse(selectedDate)
        val timestamp = Timestamp(date)

        // Create a new meeting document
        val meeting = hashMapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "date" to timestamp, // Use the selected date as timestamp
            "description" to description,
            "meetingId" to "",
            "status" to "pending", // Initial status
            "title" to title,
            "updatedAt" to "", // Initialize to empty string
            "user" to selectedUser, // Add selected user's name
            "slot" to selectedSlot // Add selected slot
        )

        db.collection("meetings")
            .add(meeting)
            .addOnSuccessListener { documentReference ->
                // Update the meeting document with the generated ID
                documentReference.update("meetingId", documentReference.id)
                    .addOnSuccessListener {
                        // Convert timestamps to readable strings
                        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        val updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        // Show toast message
                        Toast.makeText(this, "Meeting request submitted!\nCreated At: $createdAt\nUpdated At: $updatedAt", Toast.LENGTH_LONG).show()
                        // Navigate back to main activity
                        navigateBackToMainActivity()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating document: $e", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding document: $e", Toast.LENGTH_SHORT).show()
            }
    }


    private fun navigateBackToMainActivity() {
        // Create an intent to navigate back to the main activity
        val intent = Intent(this, MainActivity::class.java)
        // Start the main activity
        startActivity(intent)
        // Finish the current activity
        finish()
    }

}
