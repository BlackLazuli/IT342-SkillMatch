package com.example.skillmatch.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R

class CalendarAdapter(
    private var days: List<String>,
    private val selectedDates: Set<String>,
    private val onDateSelected: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<String>) {
        days = newDays
        notifyDataSetChanged()
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val dayCard: CardView = itemView.findViewById(R.id.dayCard)

        fun bind(day: String) {
            dayText.text = day
            
            if (day.isEmpty()) {
                dayCard.visibility = View.INVISIBLE
                dayCard.isClickable = false
            } else {
                dayCard.visibility = View.VISIBLE
                dayCard.isClickable = true
                
                // Check if this date is selected
                val isSelected = selectedDates.contains(day)
                
                if (isSelected) {
                    dayCard.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                    dayText.setTextColor(Color.WHITE)
                } else {
                    dayCard.setCardBackgroundColor(Color.WHITE)
                    dayText.setTextColor(Color.BLACK)
                }
                
                dayCard.setOnClickListener {
                    onDateSelected(day)
                }
            }
        }
    }
}