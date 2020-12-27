package spot.notify.chatbot

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_STT = 1
    }

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.UK
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_stt.setOnClickListener {
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            sttIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

            try {
                startActivityForResult(sttIntent, REQUEST_CODE_STT)
            } catch (e: ActivityNotFoundException) {
                Log.d("error",e.toString())
            }
        }

        btn_tts.setOnClickListener {
            val text = et_text_input.text.toString().trim()
            Log.d("tts",text)
            if (text.isNotEmpty()) {
              textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_STT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        et_text_input.setText(recognizedText)
                      chatbot("user",recognizedText.toString())
                    }
                }
            }
        }
    }

    override fun onPause() {
        textToSpeechEngine.stop()
        super.onPause()
    }

    override fun onDestroy() {
        textToSpeechEngine.shutdown()
        super.onDestroy()
    }


  private fun chatbot(name: String, text:String) {
    val request = ServiceBuilder.buildService(Endpoints::class.java)
    val call = request.chatbot(UserMessage(name,text))
    call.enqueue(object : Callback<List<BotResponse>> {
      override fun onResponse(call: Call<List<BotResponse>>, response: Response<List<BotResponse>>) {
        if (response.isSuccessful) {
          if (response.body() == null || response.body()!!.size == 0) {
            //TODO
          } else {
            val botResponse = response.body()!![0]
            et_text_output.setText(botResponse.text)
            textToSpeechEngine.speak(botResponse.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
          }
        }
      }

      override fun onFailure(call: Call<List<BotResponse>>, t: Throwable) {
        Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
        Log.d("error","${t.message}")
      }
    })
  }
}
