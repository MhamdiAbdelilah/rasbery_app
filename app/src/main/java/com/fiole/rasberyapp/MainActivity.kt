package com.fiole.rasberyapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

var url = "http://192.168.1.63:1880/light"

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val settingsbtn = findViewById<ImageButton>(R.id.settings_btn)

        settingsbtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val salon1: ToggleButton  = findViewById(R.id.salon_1)
        val salon2: ToggleButton = findViewById(R.id.salon_2)
        val chambre11: ToggleButton  = findViewById(R.id.chambre_1_1)
        val chambre12: ToggleButton  = findViewById(R.id.chambre_1_2)
        val chambre21: ToggleButton  = findViewById(R.id.chambre_2_1)
        val chambre22: ToggleButton  = findViewById(R.id.chambre_2_2)

        val textView: TextView = findViewById(R.id.tab_title)

        val salon1Handler = LightToggleButtonHandler(salon1, url, "salon",1)
        val salon2Handler = LightToggleButtonHandler(salon2, url, "salon",2)
        val chambre11Handler = LightToggleButtonHandler(chambre11, url, "chambre_1",1)
        val chambre12Handler = LightToggleButtonHandler(chambre12, url, "chambre_1",2)
        val chambre21Handler = LightToggleButtonHandler(chambre21, url, "chambre_2",1)
        val chambre22Handler = LightToggleButtonHandler(chambre22, url, "chambre_2",2)


    }
}

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings)

        val InputIp = findViewById<TextInputLayout>(R.id.input_ip)

        val btnIp = findViewById<Button>(R.id.btn_ip)
        btnIp.setOnClickListener{
            if (InputIp.editText?.text != null){
                url = "http://${InputIp.editText?.text}:1880/light"
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                showErrorDialog(this,"Please enter an IP address")
            }

        }
    }
}

data class RoomLightsState(
    val room: String,
    val lights: List<Int>
)

class LightToggleButtonHandler(
    private val toggleButton: ToggleButton,
    private val url: String,
    private val room: String,
    private val lightNumber: Int = 1
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            handleToggleChange(isChecked)
        }
    }

    private fun handleToggleChange(isChecked: Boolean) {
        coroutineScope.launch {
            val lightValue = if (isChecked) 1 else 0

            val response = withContext(Dispatchers.IO) {
                sendGetRequest(url, lightValue, room, lightNumber)
            }
            val responseJson = jsonToKotlin(response)
            updateToggleButtonState(responseJson)
        }
    }

    private fun updateToggleButtonState(responseJson: RoomLightsState?) {
        if (responseJson != null) {
            try {
                val lightState = responseJson.lights[lightNumber - 1]
                val backgroundResource = if (lightState == 1) {
                    R.drawable.light_btn_on
                } else {
                    R.drawable.light_btn_off
                }
                toggleButton.setBackgroundResource(backgroundResource)
            }
            catch (e: Exception) {
                showErrorDialog()
            }

        } else {
            showErrorDialog()
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(toggleButton.context)
            .setTitle("Error")
            .setMessage("Failed to parse data.")
            .setPositiveButton("OK", null)
            .show()
    }
}

fun sendGetRequest(url: String, lightValue: Int, room: String, lightnbr: Int): String {
    val client = OkHttpClient()
    val fullUrl = "$url?room=$room&light=$lightnbr&state=$lightValue"

    val request = Request.Builder()
        .url(fullUrl)
        .get()
        .build()

    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                "Error: Server responded with ${response.code}"
            } else {
                response.body?.string() ?: "Error: Empty response"
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.message}"
    }
}


fun jsonToKotlin(response: String): RoomLightsState ? {
    return try {
        val gson = Gson()
        gson.fromJson(response, RoomLightsState::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun showErrorDialog(context: Context, alertText: String?, title: String = "Error") {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(alertText ?: "An error occurred.")
        .setPositiveButton("OK", null)
        .show()
}