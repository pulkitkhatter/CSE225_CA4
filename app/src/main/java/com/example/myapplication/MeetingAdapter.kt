package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MeetingAdapter(
    private val meetings: List<Meeting>,
    private val listener: MeetingClickListener
) : RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>() {

    interface MeetingClickListener {
        fun onAcceptClick(meeting: Meeting)
        fun onRejectClick(meeting: Meeting)
    }

    inner class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewMeetingTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewMeetingDescription)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewMeetingDate)
        private val acceptButton: Button = itemView.findViewById(R.id.buttonAccept)
        private val rejectButton: Button = itemView.findViewById(R.id.buttonReject)

        fun bind(meeting: Meeting) {
            titleTextView.text = meeting.title
            descriptionTextView.text = meeting.description
            dateTextView.text = meeting.date

            acceptButton.setOnClickListener { listener.onAcceptClick(meeting) }
            rejectButton.setOnClickListener { listener.onRejectClick(meeting) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerviewitem, parent, false)
        return MeetingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = meetings[position]
        holder.bind(meeting)
    }

    override fun getItemCount(): Int {
        return meetings.size
    }
}
