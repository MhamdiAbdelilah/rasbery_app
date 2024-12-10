package com.fiole.rasberyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        val button: Button = findViewById(R.id.salon_1)
        val textView: TextView = findViewById(R.id.tab_title)


        button.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val response = withContext(Dispatchers.IO) {
                    sendGetRequest(url)
                }
                textView.text = response
            }
        }
    }
}

fun sendGetRequest (url : String ): String {
    return try {
        val rocket = URL("$url?room=salon&light=2")
        (rocket.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
        }.inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.message}"
    }
}

