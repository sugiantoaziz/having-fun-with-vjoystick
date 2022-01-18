package com.sigma.myjoysticktest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.sigma.myjoysticktest.helper.MqttHelper
import com.sigma.myjoysticktest.pojo.PayloadPublishReq


class MainViewModel : ViewModel() {
    private var mqttHelper: MqttHelper? = null
    private val subscriptionTopic = "ganesh/joystick/subs"
    private val publishingTopic = "ganesh/joystick/signal"
    val clientId = ""

    private val mapper = ObjectMapper()

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    fun connectMqtt(application: Application) {
        mqttHelper = MqttHelper.CallInstance.getInstance(application.applicationContext, clientId)
        mqttHelper!!.apply {
            subscriptionTopic = this@MainViewModel.subscriptionTopic
            publishingTopic = this@MainViewModel.publishingTopic
        }
    }

    fun publishCommand(payloadPublishReq: PayloadPublishReq?) {
        Log.i("MQTT INFO", "MQTT Object: $mqttHelper")
        val ow = mapper.writer().withDefaultPrettyPrinter()
        val jsonInString = ow.writeValueAsString(payloadPublishReq)
        mqttHelper!!.publishToTopic(jsonInString)
    }

    fun disconnect() {
        mqttHelper!!.apply {
            disconnect()
            deconstruct()
        }
    }
}