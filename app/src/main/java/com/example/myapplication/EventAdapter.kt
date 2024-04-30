package com.example.myapplication
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private val context: Context, private var eventTitles: List<String>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val eventTitle = eventTitles[position]
        holder.bind(eventTitle)
        holder.itemView.setOnClickListener {
            // Handle item click
            val intent = Intent(context, AdminMainActivity::class.java)
            intent.putExtra("eventTitle", eventTitle)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return eventTitles.size
    }

    fun updateData(newList: List<String>) {
        eventTitles = newList
        notifyDataSetChanged()
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitleTextView: TextView = itemView.findViewById(R.id.eventTitleTextView)

        fun bind(title: String) {
            eventTitleTextView.text = title
        }
    }
}
