package com.fiole.rasberyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL



class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val url = "https://37a2d40b-698f-4a31-9a50-24ddaf34f11c.mock.pstmn.io/light"
        val salon1: ToggleButton  = findViewById(R.id.salon_1)
        val salon2: ToggleButton = findViewById(R.id.salon_2)
        val chambre11: ToggleButton  = findViewById(R.id.chambre_1_1)
        val chambre12: ToggleButton  = findViewById(R.id.chambre_1_2)
        val chambre21: ToggleButton  = findViewById(R.id.chambre_2_1)
        val chambre22: ToggleButton  = findViewById(R.id.chambre_2_2)

        val textView: TextView = findViewById(R.id.tab_title)

        var room : String = "salon"
        salon1.setOnCheckedChangeListener { _, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                val lightValue :Int = if (isChecked) 1 else 0
                val response = withContext(Dispatchers.IO) {
                    sendGetRequest(url,lightValue,room,lightnbr = 1)
                }
                val responseJson : RoomLightsState? = jsonToKotlin(response)
                if (responseJson != null) {
                    if (responseJson.lights[0] == 1) {
                        salon1.setBackgroundResource(R.drawable.light_btn_on)
                    } else {
                        salon1.setBackgroundResource(R.drawable.light_btn_off)
                    }
                }else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Error")
                        .setMessage("Failed to parse data.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
        salon2.setOnCheckedChangeListener { _, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                val lightValue :Int = if (isChecked) 1 else 0
                val response = withContext(Dispatchers.IO) {
                    sendGetRequest(url,lightValue,room, lightnbr = 2)
                }
                val responseJson : RoomLightsState? = jsonToKotlin(response)
                if (responseJson != null) {
                    if (responseJson.lights[0] == 1) {
                        salon2.setBackgroundResource(R.drawable.light_btn_on)
                    } else {
                        salon2.setBackgroundResource(R.drawable.light_btn_off)
                    }
                }else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Error")
                        .setMessage("Failed to parse data.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }

    }
}


data class RoomLightsState(
    val room: String,
    val lights: List<Int>
)

fun sendGetRequest (url : String , lightValue : Int , room : String ,lightnbr : Int): String {
    return try {
        val rocket = URL("$url?room=$room&light=$lightnbr&state=$lightValue")
        (rocket.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
        }.inputStream.bufferedReader().use { it.readText() }
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
