package com.example.cuentaconmigo.core.export

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Escribe un [XlsxWorkbook] como un archivo `.xlsx` (OOXML SpreadsheetML)
 * mínimo pero válido, sin dependencias externas: solo `java.util.zip` y
 * construcción manual de XML.
 *
 * No usa `sharedStrings.xml` (todas las celdas de texto van con
 * `t="inlineStr"`): para los volúmenes de este export es innecesario y evita
 * tener que mantener sincronizada una tabla de strings compartidos aparte.
 *
 * Todos los números (`Long`/`Double`) se emiten con `toString()`, nunca con
 * `String.format`, precisamente para no depender del locale por defecto de la
 * JVM: en es-CO, `String.format("%.2f", 0.5)` da `"0,5"`, que rompe el XML de
 * SpreadsheetML (espera `.` como separador decimal).
 */
class XlsxWriter {

    fun write(workbook: XlsxWorkbook, output: OutputStream) {
        val sheetNames = XlsxSheetNameSanitizer.sanitize(workbook.sheets.map { it.name })
        val sheetCount = workbook.sheets.size

        ZipOutputStream(output).use { zip ->
            writeEntry(zip, "[Content_Types].xml", buildContentTypesXml(sheetCount))
            writeEntry(zip, "_rels/.rels", RELS_XML)
            writeEntry(zip, "xl/workbook.xml", buildWorkbookXml(sheetNames))
            writeEntry(zip, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml(sheetCount))
            writeEntry(zip, "xl/styles.xml", XlsxStyles.xml())
            workbook.sheets.forEachIndexed { index, sheet ->
                writeEntry(zip, "xl/worksheets/sheet${index + 1}.xml", buildSheetXml(sheet))
            }
        }
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildContentTypesXml(sheetCount: Int): String {
        val sheetOverrides = (1..sheetCount).joinToString(separator = "") { index ->
            "<Override PartName=\"/xl/worksheets/sheet$index.xml\" " +
                "ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
        }
        return XML_DECLARATION +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" " +
            "ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" " +
            "ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            sheetOverrides +
            "</Types>"
    }

    private fun buildWorkbookXml(sheetNames: List<String>): String {
        val sheetTags = sheetNames.mapIndexed { index, name ->
            val sheetId = index + 1
            "<sheet name=\"${XlsxXmlText.escape(name)}\" sheetId=\"$sheetId\" r:id=\"rId$sheetId\"/>"
        }.joinToString(separator = "")
        return XML_DECLARATION +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets>$sheetTags</sheets>" +
            "</workbook>"
    }

    private fun buildWorkbookRelsXml(sheetCount: Int): String {
        val sheetRels = (1..sheetCount).joinToString(separator = "") { index ->
            "<Relationship Id=\"rId$index\" " +
                "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" " +
                "Target=\"worksheets/sheet$index.xml\"/>"
        }
        val stylesRelId = sheetCount + 1
        return XML_DECLARATION +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            sheetRels +
            "<Relationship Id=\"rId$stylesRelId\" " +
            "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" " +
            "Target=\"styles.xml\"/>" +
            "</Relationships>"
    }

    private fun buildSheetXml(sheet: XlsxSheet): String {
        val rowsXml = sheet.rows.mapIndexed { rowIndex, row ->
            val rowNumber = rowIndex + 1
            val cellsXml = row.cells.mapIndexed { columnIndex, cell ->
                buildCellXml(XlsxCellReference.cellRef(columnIndex, rowNumber), cell)
            }.joinToString(separator = "")
            "<row r=\"$rowNumber\">$cellsXml</row>"
        }.joinToString(separator = "")

        return XML_DECLARATION +
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<sheetData>$rowsXml</sheetData>" +
            "</worksheet>"
    }

    private fun buildCellXml(ref: String, cell: XlsxCell): String = when (cell) {
        is XlsxCell.Empty -> ""
        is XlsxCell.Text -> inlineStringCell(ref, XlsxStyles.STYLE_GENERAL, cell.value)
        is XlsxCell.Header -> inlineStringCell(ref, XlsxStyles.STYLE_HEADER, cell.value)
        is XlsxCell.Number -> numericCell(ref, XlsxStyles.STYLE_NUMBER, cell.value.toString())
        is XlsxCell.Currency -> numericCell(ref, XlsxStyles.STYLE_CURRENCY, cell.value.toString())
        is XlsxCell.Percent -> numericCell(ref, XlsxStyles.STYLE_PERCENT, XlsxNumberText.plain(cell.value))
        is XlsxCell.Date ->
            numericCell(ref, XlsxStyles.STYLE_DATE, XlsxDateSerial.toExcelSerial(cell.value).toString())
    }

    private fun inlineStringCell(ref: String, style: Int, value: String): String =
        "<c r=\"$ref\" s=\"$style\" t=\"inlineStr\"><is><t>${XlsxXmlText.escape(value)}</t></is></c>"

    private fun numericCell(ref: String, style: Int, rawValue: String): String =
        "<c r=\"$ref\" s=\"$style\"><v>$rawValue</v></c>"

    private companion object {
        const val XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        const val RELS_XML = XML_DECLARATION +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" " +
            "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" " +
            "Target=\"xl/workbook.xml\"/>" +
            "</Relationships>"
    }
}
