package com.example.cuentaconmigo.features.transactions.voice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentaconmigo.core.network.GeminiTransactionParser
import com.example.cuentaconmigo.core.util.AudioPlayer
import com.example.cuentaconmigo.core.voice.RecognitionResult
import com.example.cuentaconmigo.core.voice.SpeechRecognizerManager
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount
import com.example.cuentaconmigo.domain.model.ParsedTransaction
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.model.missingFields
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import android.util.Log
import com.example.cuentaconmigo.core.network.RateLimitException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    data class Parsing(val transcript: String) : VoiceState()
    data class ConfirmPending(val parsed: ParsedTransaction) : VoiceState()
    data class FieldError(val missingFields: List<String>, val partial: ParsedTransaction?) : VoiceState()
    data class Error(val message: String) : VoiceState()
    object Success : VoiceState()
}

@HiltViewModel
class VoiceInputViewModel @Inject constructor(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val geminiParser: GeminiTransactionParser,
    private val transactionRepository: TransactionRepository,
    private val depositAccountRepository: DepositAccountRepository,
    private val destinationAccountRepository: DestinationAccountRepository,
    private val audioPlayer: AudioPlayer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val depositAccounts = MutableStateFlow<List<DepositAccount>>(emptyList())
    private val destinationAccounts = MutableStateFlow<List<DestinationAccount>>(emptyList())

    init {
        viewModelScope.launch {
            depositAccountRepository.getByUser(userId).collect { depositAccounts.value = it }
        }
        viewModelScope.launch {
            destinationAccountRepository.getByUser(userId).collect { destinationAccounts.value = it }
        }
    }

    fun startListening(partial: ParsedTransaction? = null) {
        _voiceState.value = VoiceState.Listening
        // SpeechRecognizer must be collected on the main thread
        viewModelScope.launch(Dispatchers.Main) {
            speechRecognizerManager.startListening().collect { result ->
                when (result) {
                    is RecognitionResult.Final -> handleTranscript(result.text, partial)
                    is RecognitionResult.Error -> _voiceState.value = VoiceState.Error(result.message)
                    is RecognitionResult.Partial -> {} // ignore partials
                }
            }
        }
    }

    private suspend fun handleTranscript(transcript: String, partial: ParsedTransaction?, retries: Int = 1) {
        _voiceState.value = VoiceState.Parsing(transcript)
        try {
            val parsed = withContext(Dispatchers.IO) {
                geminiParser.parseTranscript(
                    transcript = transcript,
                    partial = partial,
                    depositAccountNames = depositAccounts.value.map { it.name },
                    destinationAccountNames = destinationAccounts.value.map { it.name }
                )
            }
            val merged = mergeParsed(partial, parsed)
            val missing = merged.missingFields()
            if (missing.isEmpty()) {
                _voiceState.value = VoiceState.ConfirmPending(merged)
            } else {
                missing.forEach { field -> audioPlayer.speakMissingField(field) }
                _voiceState.value = VoiceState.FieldError(missing, merged)
            }
        } catch (e: RateLimitException) {
            if (retries > 0) {
                Log.w("VoiceInput", "Rate limited, retrying in 5s... ($retries left)")
                _voiceState.value = VoiceState.Parsing("$transcript (reintentando...)")
                delay(5_000)
                handleTranscript(transcript, partial, retries - 1)
            } else {
                _voiceState.value = VoiceState.Error("El servicio está ocupado. Intenta en unos segundos.")
            }
        } catch (e: Exception) {
            Log.e("VoiceInput", "Error calling LLM: ${e::class.simpleName} — ${e.message}", e)
            _voiceState.value = VoiceState.Error("Error al procesar el audio. Usa el formulario manual.")
        }
    }

    private fun mergeParsed(base: ParsedTransaction?, update: ParsedTransaction): ParsedTransaction {
        if (base == null) return update
        return ParsedTransaction(
            type = update.type ?: base.type,
            depositAccountName = update.depositAccountName ?: base.depositAccountName,
            destinationAccountName = update.destinationAccountName ?: base.destinationAccountName,
            amount = update.amount ?: base.amount,
            description = update.description ?: base.description
        )
    }

    fun confirmTransaction(parsed: ParsedTransaction) {
        val depositAccount = depositAccounts.value.firstOrNull {
            it.name.equals(parsed.depositAccountName, ignoreCase = true)
        } ?: run {
            _voiceState.value = VoiceState.Error("Cuenta de depósito no encontrada")
            return
        }

        val destinationAccount = if (parsed.type == TransactionType.EXPENSE) {
            destinationAccounts.value.firstOrNull {
                it.name.equals(parsed.destinationAccountName, ignoreCase = true)
            } ?: run {
                _voiceState.value = VoiceState.Error("Cuenta de destino no encontrada")
                return
            }
        } else null

        viewModelScope.launch {
            runCatching {
                transactionRepository.insert(
                    Transaction(
                        id = 0,
                        userId = userId,
                        depositAccountId = depositAccount.id,
                        destinationAccountId = destinationAccount?.id,
                        type = parsed.type!!,
                        amount = parsed.amount!!,
                        date = LocalDate.now(),
                        description = parsed.description
                    )
                )
            }
                .onSuccess { _voiceState.value = VoiceState.Success }
                .onFailure { _voiceState.value = VoiceState.Error(it.message ?: "Error al guardar") }
        }
    }

    fun reset() { _voiceState.value = VoiceState.Idle }
}