package com.sigma.myjoysticktest

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sigma.myjoysticktest.databinding.ActivityMainBinding
import com.sigma.myjoysticktest.pojo.PayloadPublishReq
import com.sigma.myjoysticktest.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    private var payload: PayloadPublishReq? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.connectMqtt(application)

        binding.joystickViewLeft.setOnMoveListener { angle, strength ->
            binding.textViewAngleLeft.text = "{$angle}°"
            binding.textViewStrengthLeft.text = "${strength}%"
        }
        binding.joystickViewRight.setOnMoveListener { angle, strength ->
            binding.apply {
                textViewAngleRight.text = "{$angle}°"
                textViewStrengthRight.text = "{$strength%}"
                textViewCoordinateRight.text = String.format(
                    "x%03d:y%03d", joystickViewRight.normalizedX, joystickViewRight.normalizedY
                )
                payload = PayloadPublishReq(
                    textViewAngleLeft.text.toString(),
                    textViewStrengthLeft.text.toString(),
                    textViewAngleRight.text.toString(),
                    textViewStrengthRight.text.toString(),
                    textViewCoordinateRight.text.toString()
                )
            }
            mainViewModel.publishCommand(payload!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.disconnect()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.connectMqtt(application)
    }

}