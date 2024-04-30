package com.example.myapplication

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var selectedDateTextView: TextView
    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userSpinner: Spinner
    private lateinit var noEventsTextView: TextView

    private lateinit var fabMain: FloatingActionButton
    private var isRotated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        fabMain = findViewById(R.id.fabMain)
        val expandedButtonsLayout = findViewById<View>(R.id.expandedButtonsLayout)

        fabMain.setOnClickListener {
            toggleExpandedButtons(expandedButtonsLayout)
            rotateFabIcon()
        }

        findViewById<FloatingActionButton>(R.id.fabPlus).setOnClickListener {
            val intent = Intent(this, MakingMeetingRequest::class.java)
            startActivity(intent)
        }

        val fabMeeting = findViewById<FloatingActionButton>(R.id.fabMeeting)
        fabMeeting.setOnClickListener {
            val intent = Intent(this@MainActivity, MeetingRequest::class.java)
            startActivity(intent)
        }

        val fabReminder = findViewById<FloatingActionButton>(R.id.fabReminder)
        fabReminder.setOnClickListener {
            performLogout()
        }

        selectedDateTextView = findViewById(R.id.selectedDateTextView)
        calendarView = findViewById(R.id.calendarView)
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        noEventsTextView = findViewById(R.id.noEventsTextView)

        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(this, emptyList())
        eventsRecyclerView.adapter = eventAdapter

        // Set the initial text of selectedDateTextView to today's date
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayDate = sdf.format(currentDate)
        selectedDateTextView.text = todayDate

        calendarView.setOnDayClickListener(object :
            com.applandeo.materialcalendarview.listeners.OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val date = eventDay.calendar.time
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = sdf.format(date)
                selectedDateTextView.text = "$selectedDate"
                val selectedUser = userSpinner.selectedItem.toString()
                fetchEventsForDate(selectedDate, selectedUser)
            }
        })

        // Spinner setup
        userSpinner = findViewById(R.id.userSpinner)
        val users = arrayOf("Everyone", "Deepak", "Naresh", "Pulkit", "Ketan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, users)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userSpinner.adapter = adapter
        userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if(selectedItem != "All") {
                    loadAcceptedEventDates(selectedItem)
                } else {
                    // Load all accepted event dates
                    loadAcceptedEventDates()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Initialize no events text view
        noEventsTextView.visibility = View.GONE

        // Load initial data for all accepted event dates
        loadAcceptedEventDates()
    }

    private fun toggleExpandedButtons(expandedButtonsLayout: View) {
        val isExpanded = expandedButtonsLayout.visibility == View.VISIBLE
        expandedButtonsLayout.visibility = if (isExpanded) View.GONE else View.VISIBLE
    }

    private fun loadAcceptedEventDates(selectedUser: String? = null) {
        val db = FirebaseFirestore.getInstance()
        db.collection("meetings")
            .whereEqualTo("status", "Accepted")
            .let { query ->
                if (selectedUser != null && selectedUser != "Everyone") {
                    query.whereEqualTo("user", selectedUser)
                } else {
                    query
                }
            }
            .get()
            .addOnSuccessListener { documents ->
                val calendarDays = mutableListOf<EventDay>()

                for (document in documents) {
                    val timestamp = document.getTimestamp("date")
                    timestamp?.let {
                        try {
                            val date = timestamp.toDate()
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            val plusIcon = resources.getDrawable(R.drawable.baseline_circle_24)
                            val eventDay = EventDay(calendar, plusIcon)
                            calendarDays.add(eventDay)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                Log.d("MainActivity", "Loaded ${calendarDays.size} event days")
                calendarView.setEvents(calendarDays)

                // Check if there are events for the selected date
                val selectedDate = selectedDateTextView.text.toString()
                val selectedUser = userSpinner.selectedItem.toString()
                fetchEventsForDate(selectedDate, selectedUser)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun fetchEventsForDate(selectedDate: String, selectedUser: String?) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDate = sdf.parse(selectedDate)
        val selectedTimestamp = Timestamp(parsedDate!!.time / 1000, 0)
        val db = FirebaseFirestore.getInstance()

        var query = db.collection("meetings")
            .whereEqualTo("date", selectedTimestamp)

        if (selectedUser != null && selectedUser != "Everyone") {
            query = query.whereEqualTo("user", selectedUser)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val eventTitles = mutableListOf<String>()
                for (document in documents) {
                    val status = document.getString("status")
                    if (status == "Accepted") {
                        val title = document.getString("title")
                        title?.let {
                            eventTitles.add(it)
                        }
                    }
                }
                updateRecyclerView(eventTitles)

                // Show or hide "No events" text view based on data availability
                if (eventTitles.isEmpty()) {
                    noEventsTextView.visibility = View.VISIBLE
                } else {
                    noEventsTextView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun performLogout() {
        firebaseAuth.signOut()
        val intent = Intent(this@MainActivity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun updateRecyclerView(eventTitles: List<String>) {
        eventAdapter.updateData(eventTitles)
    }

    private fun rotateFabIcon() {
        val rotation = if (isRotated) 0f else 180f
        val animator = ObjectAnimator.ofFloat(fabMain, View.ROTATION, rotation)
        animator.duration = 300
        animator.start()
        isRotated = !isRotated
    }
}
