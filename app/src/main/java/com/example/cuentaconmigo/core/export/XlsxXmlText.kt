package com.example.cuentaconmigo.core.export

/**
 * Prepara texto arbitrario (nombres de categoría, descripciones de transacción
 * escritos por el usuario) para insertarlo dentro de un `<is><t>...</t></is>`
 * de SpreadsheetML.
 *
 * Hace dos cosas, en este orden:
 * 1. Descarta caracteres de control ilegales en XML 1.0 (cualquier code point
 *    menor a 0x20 salvo tab/LF/CR). Si se emiten tal cual, Excel no los rechaza
 *    con un error visible: el archivo queda silenciosamente corrupto.
 * 2. Escapa las cinco entidades XML obligatorias (`&` primero, para no volver a
 *    escapar los `&` que genera el propio escapado de `<`, `>`, etc.).
 */
object XlsxXmlText {

    fun escape(raw: String): String {
        val withoutIllegalChars = raw.filter { isLegalXmlChar(it) }
        return withoutIllegalChars
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun isLegalXmlChar(char: Char): Boolean =
        char == '\t' || char == '\n' || char == '\r' || char.code >= 0x20
}
