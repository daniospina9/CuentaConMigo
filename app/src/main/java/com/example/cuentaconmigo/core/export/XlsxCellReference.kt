package com.example.cuentaconmigo.core.export

/**
 * Conversión entre índices de columna base 0 y las referencias de celda que
 * usa SpreadsheetML (`A1`, `B1`, ..., `Z1`, `AA1`, `AB1`, ...).
 *
 * Es aritmética de numeración posicional en base 26 SIN el dígito cero (bijective
 * base-26): por eso no alcanza con un `Int.toString(26)` ni un módulo simple, y el
 * caso de más de 26 columnas (`AA`, `AB`, ...) es el error clásico si no se resta 1
 * en cada iteración antes de dividir.
 */
object XlsxCellReference {

    /** @param columnIndex índice de columna base 0 (0 = A, 25 = Z, 26 = AA, ...). */
    fun columnLetters(columnIndex: Int): String {
        var remaining = columnIndex + 1 // se trabaja en base 1 para el algoritmo bijective
        val letters = StringBuilder()
        while (remaining > 0) {
            val digit = (remaining - 1) % 26
            letters.insert(0, ('A' + digit))
            remaining = (remaining - 1) / 26
        }
        return letters.toString()
    }

    /** Referencia de celda completa, ej. `columnIndex=26, rowNumber=1` -> `"AA1"`. */
    fun cellRef(columnIndex: Int, rowNumber: Int): String =
        columnLetters(columnIndex) + rowNumber.toString()
}
