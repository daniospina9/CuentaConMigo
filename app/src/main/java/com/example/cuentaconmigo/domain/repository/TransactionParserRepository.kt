package com.example.cuentaconmigo.domain.repository

import com.example.cuentaconmigo.domain.model.ParsedTransaction

interface TransactionParserRepository {
    suspend fun parseTranscript(
        transcript: String,
        partial: ParsedTransaction?,
        depositAccountNames: List<String>,
        destinationAccountNames: List<String>
    ): ParsedTransaction
}