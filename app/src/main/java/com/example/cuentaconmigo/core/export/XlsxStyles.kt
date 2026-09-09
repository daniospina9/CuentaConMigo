package com.example.cuentaconmigo.core.export

/**
 * Contenido de `xl/styles.xml` y los índices de `cellXfs` que usa [XlsxWriter]
 * para asignarle estilo a cada [XlsxCell].
 *
 * Excel es estricto con este archivo. Dos detalles que rompen (o hacen que
 * Excel "repare") un xlsx armado a mano si se los simplifica:
 * - `<fills>` debe declarar como PRIMERAS DOS entradas `patternType="none"` y
 *   `patternType="gray125"`, en ese orden, existan o no fills custom después.
 * - Los `numFmtId` custom deben ser >= 164; los menores están reservados para
 *   formatos built-in de Excel.
 * - Debe declarar `<cellStyles>` con el estilo `Normal` (builtinId 0): sin él,
 *   Excel puede ofrecer "reparar" el archivo al abrirlo.
 *
 * El orden de las secciones también es fijo por el schema de SpreadsheetML:
 * `numFmts`, `fonts`, `fills`, `borders`, `cellStyleXfs`, `cellXfs`, `cellStyles`.
 */
object XlsxStyles {

    // Índices dentro de <cellXfs>, en el mismo orden en que se declaran abajo.
    // XlsxWriter usa estas constantes como atributo `s` de cada celda.
    const val STYLE_GENERAL = 0
    const val STYLE_NUMBER = 1
    const val STYLE_CURRENCY = 2
    const val STYLE_PERCENT = 3
    const val STYLE_DATE = 4
    const val STYLE_HEADER = 5

    private const val NUM_FMT_CURRENCY = 164
    private const val NUM_FMT_PERCENT = 165
    private const val NUM_FMT_DATE = 166

    fun xml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="3">
<numFmt numFmtId="$NUM_FMT_CURRENCY" formatCode="#,##0"/>
<numFmt numFmtId="$NUM_FMT_PERCENT" formatCode="0.0%"/>
<numFmt numFmtId="$NUM_FMT_DATE" formatCode="dd/mm/yyyy"/>
</numFmts>
<fonts count="2">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><name val="Calibri"/></font>
</fonts>
<fills count="2">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
</fills>
<borders count="1">
<border><left/><right/><top/><bottom/><diagonal/></border>
</borders>
<cellStyleXfs count="1">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
</cellStyleXfs>
<cellXfs count="6">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="1" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
<xf numFmtId="$NUM_FMT_CURRENCY" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
<xf numFmtId="$NUM_FMT_PERCENT" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
<xf numFmtId="$NUM_FMT_DATE" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
</cellXfs>
<cellStyles count="1">
<cellStyle name="Normal" xfId="0" builtinId="0"/>
</cellStyles>
</styleSheet>
"""
}
