package ro.pub.cs.systems.eim.practicaltest02v2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class PracticalTest02v2MainActivity : AppCompatActivity() {
    private lateinit var definitionReceiver: DefinitionBroadcastReceiver
    private lateinit var rezultat: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practical_test02v2_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cuvantText: EditText = findViewById(R.id.cuvant)
        val button: Button = findViewById(R.id.button)
        rezultat = findViewById(R.id.raspuns)

        definitionReceiver = DefinitionBroadcastReceiver(rezultat)
        val intentFilter = IntentFilter("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION_BROADCAST")
        registerReceiver(definitionReceiver, intentFilter, RECEIVER_NOT_EXPORTED)

        button.setOnClickListener {
            val cuvantText: EditText = findViewById(R.id.cuvant)
            val url: String = "https://api.dictionaryapi.dev/api/v2/entries/en/" + cuvantText.text.toString()
            Log.d("URL", "URL: $url")

            getResponse(url, rezultat)
        }

        val ceas_button: Button = findViewById(R.id.ceas_button)
        ceas_button.setOnClickListener {
            val intent = Intent(this, TimeMainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getResponse(myUrl: String, rezultat: TextView) {
        thread {
            try {
                val url = URL(myUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("Raspuns_server", "Response: $responseText")

                    val definition = parseDefinition(responseText)
                    Log.d("Raspuns_parsat", "Definition: $definition")
                    val broadcastIntent = Intent("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION_BROADCAST")
                    broadcastIntent.putExtra("definition", definition)
                    sendBroadcast(broadcastIntent)
                    rezultat.text = definition
//                    runOnUiThread {
//                        Log.d("Raspuns_parsat", "Definition: $definition")
//                        val broadcastIntent = Intent("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION_BROADCAST")
//                        broadcastIntent.putExtra("definition", definition)
//                        sendBroadcast(broadcastIntent)
//                        //rezultat.text = definition
//                    }
                } else {
                    val errorMessage = "Cuvântul nu a fost găsit! Cod răspuns: $responseCode"
                    val broadcastIntent = Intent("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION_BROADCAST")
                    broadcastIntent.putExtra("definition", errorMessage)
                    sendBroadcast(broadcastIntent)
//                    Log.e("FetchDefinition", errorMessage)
//                    runOnUiThread {
//                        rezultat.text = "Cuvântul nu a fost găsit!"
//                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Eroare la conectare: ${e.message}"
                val broadcastIntent = Intent("ro.pub.cs.systems.eim.practicaltest02v2.DEFINITION_BROADCAST")
                broadcastIntent.putExtra("definition", errorMessage)
                sendBroadcast(broadcastIntent)
//                Log.e("FetchDefinition", "Error: ${e.message}")
//                runOnUiThread {
//                    rezultat.text = "Eroare la conectare!"
//                }
            }
        }
    }

    private fun parseDefinition(jsonResponse: String): String {
        return try {
            val jsonArray = JSONArray(jsonResponse)
            val firstDefinition = jsonArray.getJSONObject(0)
                .getJSONArray("meanings")
                .getJSONObject(0)
                .getJSONArray("definitions")
                .getJSONObject(0)
                .getString("definition")
            firstDefinition
        } catch (e: Exception) {
            "Nu s-a putut extrage definiția."
        }
    }
}

class DefinitionBroadcastReceiver(private val definitionTextView: TextView) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val definition = intent.getStringExtra("definition") ?: "Definiția nu a fost primită."
        definitionTextView.text = definition
    }
}