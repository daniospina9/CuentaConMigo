package com.example.cuentaconmigo.core.export

import java.math.BigDecimal

/**
 * Convierte un `Double` al texto que va dentro de un `<v>` de SpreadsheetML.
 *
 * Resuelve dos problemas que `Double.toString()` no cubre:
 * 1. `toString()` pasa a notación científica fuera del rango [1e-3, 1e7):
 *    `0.0001` sale como `"1.0E-4"`. El schema lo admite (es un `xs:double`),
 *    pero no todos los consumidores del archivo lo interpretan bien, así que
 *    se emite siempre en decimal plano.
 * 2. Nunca se usa `String.format`, porque tomaría el locale por defecto: en
 *    es-CO daría coma decimal y eso sí rompe el XML.
 *
 * Los valores no finitos (`NaN`, infinitos) no tienen representación válida en
 * SpreadsheetML: se emiten como `0` para no generar un archivo ilegible.
 */
object XlsxNumberText {

    fun plain(value: Double): String =
        if (!value.isFinite()) "0" else BigDecimal.valueOf(value).toPlainString()
}
