package com.sigma.myjoysticktest.helper

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttPersistenceException
import kotlin.Throws
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import java.io.UnsupportedEncodingException
import java.lang.Exception

class MqttHelper private constructor(context: Context, clientId: String) {
    var mqttAndroidClient: MqttAndroidClient
    val serverUri = "tcp://broker.emqx.io:1883"
    var subscriptionTopic: String? = null
    var publishingTopic: String? = null
    val username = ""
    val password = ""
    fun deconstruct() {
        if (instance != null) {
            try {
                instance!!.finalize()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
            instance = null
            System.gc()
        }
    }

    fun setCallback(callback: MqttCallbackExtended?) {
        mqttAndroidClient.setCallback(callback)
    }

    private fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = true
        mqttConnectOptions.userName = username
        mqttConnectOptions.password = password.toCharArray()
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                    subscribeToTopic(subscriptionTopic)
                    Log.d("MqttHelper","Successfully subscribe to topic: ${subscriptionTopic}")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.w("Mqtt", "Failed to connect to: $serverUri$exception")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun subscribeToTopic(subscriptionTopic: String?) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.w("Mqtt", "Subscribed!")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.w("Mqtt", "Subscribed fail!")
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exception subscribing")
            ex.printStackTrace()
        }
    }

    fun publishToTopic(payload: String) {
        try {
            if (!mqttAndroidClient.isConnected) {
                mqttAndroidClient.connect()
            }
            Log.w("Msg to Server: ", payload)
            val message = MqttMessage()
            try {
                message.payload = payload.toByteArray(charset("UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                Log.e("Error Encoding Char ", e.message!!)
                e.printStackTrace()
            }
            message.qos = 0
            mqttAndroidClient.publish(publishingTopic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i("INFO MQTT", "Sending message successful $payload")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.i("INFO MQTT", "publish failed!")
                }
            })
        } catch (e: MqttPersistenceException) {
            Log.e("ERROR MQTT", "Mqtt Persistence Failure " + e.message)
            e.printStackTrace()
        } catch (ex: MqttException) {
            Log.e("ERROR MQTT", "Exception publishing! " + ex.message)
            ex.printStackTrace()
        }
    }

    fun disconnect() {
        mqttAndroidClient.unregisterResources()
        mqttAndroidClient.close()
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        Log.i("DESTROY", "Object is destroyed by the Garbage Collector.")
    }

    companion object CallInstance {
        @Volatile private var instance: MqttHelper? = null
        fun getInstance(context: Context, clientId: String): MqttHelper? {
            return instance ?: synchronized(this) {
                instance ?: MqttHelper(context, clientId).also { instance = it }
            }
        }
    }

    init {
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                Log.w("mqtt", serverURI)
            }

            override fun connectionLost(cause: Throwable) {}
            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.w("Mqtt", message.toString())
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })
        connect()
    }
}