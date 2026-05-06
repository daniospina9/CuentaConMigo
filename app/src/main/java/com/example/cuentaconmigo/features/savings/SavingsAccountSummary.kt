package com.example.cuentaconmigo.features.savings

import com.example.cuentaconmigo.domain.model.DestinationAccount

data class SavingsAccountSummary(
    val account: DestinationAccount,
    val balance: Long
)