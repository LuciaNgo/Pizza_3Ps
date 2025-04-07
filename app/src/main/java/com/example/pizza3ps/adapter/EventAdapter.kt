package com.example.pizza3ps.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.pizza3ps.R
import com.example.pizza3ps.fragment.FoodInfoFragment
import com.example.pizza3ps.model.EventData

class EventAdapter(private val eventList: List<EventData>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventImageView: ImageView = view.findViewById(R.id.event_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        Glide.with(holder.itemView.context)
            .load(event.imgPath)
            .placeholder(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.eventImageView)

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("eventTitle", event.name)
                putString("eventImage", event.imgPath)
                putString("eventDescription", event.description)
            }

            holder.itemView.findNavController().navigate(
                R.id.action_dashboardFragment_to_eventInfoFragment,
                bundle
            )
        }
    }

    override fun getItemCount() = eventList.size
}