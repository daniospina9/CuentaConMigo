package com.example.cuentaconmigo.features.reports.export

import android.content.Context
import com.example.cuentaconmigo.core.export.XlsxWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Escribe el informe personalizado exportado como archivo `.xlsx` en
 * `cacheDir/exports`.
 *
 * Es deliberadamente delgada: toda la lógica que podría fallar (armar el workbook,
 * generar el nombre de archivo, escribir el XML del `.xlsx`) vive en piezas puras ya
 * testeadas ([FinancialReportWorkbookBuilder], [FinancialReportFileName],
 * [XlsxWriter]). Acá no queda nada que justifique una prueba de instrumentación.
 */
@Singleton
class FinancialReportExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val exportsDir: File
        get() = File(context.cacheDir, "exports").apply { mkdirs() }

    suspend fun export(data: FinancialReportExportData): File = withContext(Dispatchers.IO) {
        val dir = exportsDir
        // Es caché, no archivos del usuario: se limpian los exports anteriores antes
        // de escribir el nuevo (mismo criterio que DatabaseBackupManager.exportBackup()).
        dir.listFiles()?.forEach { it.delete() }

        val workbook = FinancialReportWorkbookBuilder.build(data)
        val fileName = FinancialReportFileName.forPeriod(data.startDate, data.endDate)
        val file = File(dir, fileName)
        file.outputStream().use { XlsxWriter().write(workbook, it) }
        file
    }
}
