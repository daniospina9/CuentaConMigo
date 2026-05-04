package com.example.cuentaconmigo.core.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

private val copFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

// amounts are stored as centavos — divide by 100 to display
fun Long.toCopString(): String = "$ ${copFormat.format(this / 100.0)}"

fun Long.toSignedCopString(): String =
    if (this >= 0) "+ ${toCopString()}" else "- ${(-this).toCopString()}"

// Accepts "1500" or "1500,50" → centavos (150000 or 150050)
fun String.parseToCentavos(): Long? {
    val normalized = this.trim().replace(",", ".")
    return normalized.toDoubleOrNull()
        ?.let { (it * 100).roundToLong() }
        ?.takeIf { it > 0 }
}

// Call from onValueChange: only digits + one comma + max 2 decimal places
fun filterAmountInput(current: String, new: String): String {
    val filtered = new.filter { it.isDigit() || it == ',' }
    if (filtered.count { it == ',' } > 1) return current
    val parts = filtered.split(',')
    if (parts.size == 2 && parts[1].length > 2) return current
    return filtered
}
