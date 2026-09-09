package com.example.cuentaconmigo.core.export

import java.time.LocalDate

/**
 * Excel guarda las fechas como un número de serie: días transcurridos desde
 * 1899-12-30. Ese offset (en vez de 1900-01-01) es intencional: absorbe el bug
 * histórico de Lotus 1-2-3 que trataba a 1900 como año bisiesto, y es el mismo
 * offset que usa Excel real, así que un serial calculado así abre correctamente.
 */
object XlsxDateSerial {

    private val EXCEL_EPOCH: LocalDate = LocalDate.of(1899, 12, 30)

    fun toExcelSerial(date: LocalDate): Long = date.toEpochDay() - EXCEL_EPOCH.toEpochDay()
}
