package com.example.cuentaconmigo.core.util

import java.text.NumberFormat
import java.util.Locale

private val copFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}

fun Long.toCopString(): String = "$ ${copFormat.format(this)}"

fun Long.toSignedCopString(): String = if (this >= 0) "+ ${toCopString()}" else "- ${(-this).toCopString()}"
