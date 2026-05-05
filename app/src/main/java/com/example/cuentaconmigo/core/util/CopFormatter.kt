package com.example.cuentaconmigo.core.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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

// Accepts "1.500" or "1.500,50" → centavos (150000 or 150050)
fun String.parseToCentavos(): Long? {
    val normalized = this.trim().replace(".", "").replace(",", ".")
    return normalized.toDoubleOrNull()
        ?.let { (it * 100).roundToLong() }
        ?.takeIf { it > 0 }
}

// TextFieldValue overload: keeps cursor at end after formatting
fun filterAmountInput(current: TextFieldValue, new: TextFieldValue): TextFieldValue {
    val formatted = filterAmountInput(current.text, new.text)
    return new.copy(text = formatted, selection = TextRange(formatted.length))
}

// Call from onValueChange: formats with thousand-separator dots while typing
// e.g. "1000000" → "1.000.000", "1500,50" → "1.500,50"
fun filterAmountInput(current: String, new: String): String {
    val raw = new.replace(".", "")
    val filtered = raw.filter { it.isDigit() || it == ',' }
    if (filtered.count { it == ',' } > 1) return current
    val parts = filtered.split(',')
    if (parts.size == 2 && parts[1].length > 2) return current
    val formattedInt = parts[0]
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return if (parts.size == 2) "$formattedInt,${parts[1]}" else formattedInt
}
