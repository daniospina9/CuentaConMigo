package com.example.cuentaconmigo.core.export

/** Un libro de cálculo completo: cero o más hojas, en el orden en que se escriben. */
data class XlsxWorkbook(val sheets: List<XlsxSheet>)
