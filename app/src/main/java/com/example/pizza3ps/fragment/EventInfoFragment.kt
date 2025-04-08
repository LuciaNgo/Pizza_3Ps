package com.example.pizza3ps.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.pizza3ps.R

class EventInfoFragment : Fragment() {
    private lateinit var eventTitle: String
    private lateinit var eventImage: String
    private lateinit var eventDescription: String
    private lateinit var eventTitleTextView: TextView
    private lateinit var eventImageView: ImageView
    private lateinit var backImageView: ImageView
    private lateinit var webView : WebView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventTitle = it.getString("eventTitle").orEmpty()
            eventImage = it.getString("eventImage").orEmpty()
            eventDescription = it.getString("eventDescription").orEmpty()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventTitleTextView = view.findViewById(R.id.event_title)
        eventImageView = view.findViewById(R.id.event_image)
        backImageView = view.findViewById(R.id.back_button)
        webView = view.findViewById(R.id.event_webView)

        eventTitleTextView.text = eventTitle

        // Load the image using Glide or any other image loading library
        Glide.with(this)
            .load(eventImage)
            .placeholder(R.drawable.placeholder_image)
            .into(eventImageView)

        webView.settings.javaScriptEnabled = false
        webView.settings.domStorageEnabled = true
        webView.loadDataWithBaseURL(
            null,
            eventDescription,
            "text/html",
            "UTF-8",
            null
        )

        backImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}