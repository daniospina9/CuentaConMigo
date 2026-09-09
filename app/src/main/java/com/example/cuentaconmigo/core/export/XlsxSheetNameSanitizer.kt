package com.example.cuentaconmigo.core.export

/**
 * Sanitiza nombres de hoja para SpreadsheetML: Excel prohíbe `: \ / ? * [ ]` en
 * el nombre y lo limita a 31 caracteres. En vez de lanzar una excepción cuando
 * un nombre no es válido (el caso típico: nombre de reporte generado con la
 * fecha, ej. "Enero/2025"), se reemplazan los caracteres prohibidos por `_` y
 * se trunca.
 *
 * Si, después de sanitizar, dos hojas quedan con el mismo nombre (dos reportes
 * truncados al mismo prefijo, por ejemplo), la segunda y las siguientes reciben
 * un sufijo `" (n)"`, recortando el nombre base lo necesario para no superar
 * los 31 caracteres. Este comportamiento es una decisión de diseño explícita:
 * preferir un nombre desambiguado antes que fallar o sobrescribir una hoja.
 */
object XlsxSheetNameSanitizer {

    private const val MAX_LENGTH = 31
    private val FORBIDDEN_CHARS = charArrayOf(':', '\\', '/', '?', '*', '[', ']')

    fun sanitize(names: List<String>): List<String> {
        val used = mutableSetOf<String>()
        return names.map { raw ->
            val candidate = replaceForbiddenChars(raw).take(MAX_LENGTH)
            val finalName = disambiguate(candidate, used)
            used += finalName
            finalName
        }
    }

    private fun replaceForbiddenChars(raw: String): String =
        raw.map { if (it in FORBIDDEN_CHARS) '_' else it }.joinToString(separator = "")

    private fun disambiguate(candidate: String, used: Set<String>): String {
        if (candidate !in used) return candidate

        var suffixIndex = 2
        while (true) {
            val suffix = " ($suffixIndex)"
            val maxBaseLength = (MAX_LENGTH - suffix.length).coerceAtLeast(0)
            val attempt = candidate.take(maxBaseLength) + suffix
            if (attempt !in used) return attempt
            suffixIndex++
        }
    }
}
