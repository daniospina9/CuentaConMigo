package com.example.cuentaconmigo.core.export

import java.time.LocalDate

/**
 * Modelo de una celda de una hoja de cálculo, independiente del formato de
 * archivo concreto que la termine escribiendo (`XlsxWriter` es quien sabe cómo
 * volcar cada variante a SpreadsheetML).
 */
sealed class XlsxCell {

    /** Texto libre (nombre de categoría, descripción de transacción, etc.). */
    data class Text(val value: String) : XlsxCell()

    /** Entero sin formato monetario ni de fecha (ej. una cantidad, un conteo). */
    data class Number(val value: Long) : XlsxCell()

    /**
     * Monto en pesos colombianos. Se guarda como `Long` porque en esta app los
     * montos no tienen centavos; el formato `#,##0` solo afecta cómo se muestra.
     */
    data class Currency(val value: Long) : XlsxCell()

    /**
     * Porcentaje expresado como FRACCIÓN, no como número ya multiplicado por
     * 100: `0.3` representa 30%, porque el formato de celda `0.0%` es quien se
     * encarga de multiplicar por 100 al mostrarlo. Pasar `30.0` acá duplicaría
     * ese factor y el valor mostrado saldría 100 veces más grande.
     */
    data class Percent(val value: Double) : XlsxCell()

    /** Fecha de calendario, sin componente de hora. */
    data class Date(val value: LocalDate) : XlsxCell()

    /** Celda de encabezado: mismo contenido de texto que [Text] pero en negrita. */
    data class Header(val value: String) : XlsxCell()

    /** Celda sin contenido: no se emite ningún `<c>` para ella, solo ocupa la columna. */
    object Empty : XlsxCell()
}
