package com.example.pizza3ps.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.pizza3ps.R
import android.widget.Button
import com.example.pizza3ps.activity.CustomizePizzaActivity
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class CustomizeFragment : Fragment() {
    private lateinit var konfettiView: KonfettiView
    private lateinit var customizeButton: Button
    private lateinit var backButton: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customize, container, false)

        konfettiView = view.findViewById(R.id.konfettiView)
        customizeButton = view.findViewById(R.id.customize_button)
        backButton = view.findViewById(R.id.back_button)

        customizeButton.setOnClickListener {
            konfettiView.start(
                Party(
                    speed = 30f,
                    maxSpeed = 50f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(100),
                    position = Position.Relative(0.5, 0.3)
                )
            )

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(requireContext(), CustomizePizzaActivity::class.java)
                startActivity(intent)
            }, 2500)

        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }


}