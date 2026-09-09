package com.example.cuentaconmigo.core.export

/**
 * Una hoja del workbook. [name] es el nombre "deseado"; [XlsxWriter] lo pasa
 * por [XlsxSheetNameSanitizer] antes de escribirlo, así que este nombre puede
 * contener caracteres inválidos para Excel sin que sea responsabilidad del
 * llamador sanitizarlo de antemano.
 */
data class XlsxSheet(val name: String, val rows: List<XlsxRow>)
