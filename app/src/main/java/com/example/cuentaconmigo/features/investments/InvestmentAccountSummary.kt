package com.example.cuentaconmigo.features.investments

import com.example.cuentaconmigo.domain.model.DestinationAccount

data class InvestmentAccountSummary(
    val account: DestinationAccount,
    val primaryValue: Long
)
