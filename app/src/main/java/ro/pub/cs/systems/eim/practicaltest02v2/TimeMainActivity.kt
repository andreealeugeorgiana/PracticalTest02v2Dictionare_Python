package ro.pub.cs.systems.eim.practicaltest02v2

import android.os.Bundle
import android.util.Log
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
import java.net.Socket

class TimeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_time_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val clock: TextView = findViewById(R.id.clock)
        val button: Button = findViewById(R.id.button2)

        button.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("SocketDebug", "Attempting to connect to server...")
                    val socket = Socket("10.0.2.2", 12345)
                    Log.d("SocketDebug", "Connected to server.")

                    // Read response from the server
                    val input = socket.getInputStream().bufferedReader().use { it.readLine() }

                    // Update the UI with the received response
                    withContext(Dispatchers.Main) {
                        clock.text = input ?: "No response from server"
                    }

                    // Close the socket
                    socket.close()
                    Log.d("SocketDebug", "Socket closed.")
                } catch (e: Exception) {
                    Log.e("SocketError", "Error: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        clock.text = "Eroare la conectare"
                    }
                }
            }
        }
    }
}