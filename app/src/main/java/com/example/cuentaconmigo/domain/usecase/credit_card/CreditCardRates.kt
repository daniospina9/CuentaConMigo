package com.example.cuentaconmigo.domain.usecase.credit_card

/** Monthly effective rate (TEM) from an annual effective rate expressed in percent. */
fun monthlyEffectiveRate(annualRatePercent: Double): Double =
    Math.pow(1.0 + annualRatePercent / 100.0, 1.0 / 12.0) - 1.0
