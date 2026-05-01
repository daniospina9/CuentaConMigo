package com.example.cuentaconmigo.core.util

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "CO")
                ready = true
            }
        }
    }

    fun speak(message: String) {
        if (ready) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun speakMissingField(field: String) {
        val message = when (field) {
            "type"               -> "No has indicado si es un ingreso o un gasto."
            "depositAccount"     -> "No has especificado una cuenta de depósito válida."
            "destinationAccount" -> "No has especificado una cuenta de destino válida."
            "amount"             -> "No has indicado el monto de la transacción."
            else                 -> "Falta información en tu mensaje de voz."
        }
        speak(message)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
