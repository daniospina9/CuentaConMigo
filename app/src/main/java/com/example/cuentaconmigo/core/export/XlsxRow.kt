package com.example.cuentaconmigo.core.export

/**
 * Una fila de una hoja: lista de celdas en el orden en que deben mostrarse.
 * Filas de distinto largo (ragged) son válidas: cada [XlsxSheet] puede tener
 * filas con distinta cantidad de celdas sin que eso rompa la escritura.
 */
data class XlsxRow(val cells: List<XlsxCell>)
