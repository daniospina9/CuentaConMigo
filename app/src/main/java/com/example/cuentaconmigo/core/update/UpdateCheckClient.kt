package com.example.cuentaconmigo.core.update

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Busca el manifiesto remoto de versión e informa si hay una actualización disponible.
 * Reutiliza el [OkHttpClient] singleton cableado en
 * [com.example.cuentaconmigo.core.di.NetworkModule] en vez de crear uno nuevo.
 *
 * Es un chequeo manual, disparado por el usuario: nada de acá descarga el APK ni
 * dispara una instalación — el `apkUrl` del manifiesto solo lo usa la capa de UI para
 * abrir el navegador vía `Intent.ACTION_VIEW`.
 */
@Singleton
class UpdateCheckClient @Inject constructor(
    private val client: OkHttpClient,
    @Named("update_manifest_url") private val manifestUrl: String
) {

    /** Nunca lanza: cualquier falla de red, HTTP o de parseo se convierte en [UpdateCheckResult.Failed]. */
    suspend fun checkForUpdate(installedVersionCode: Int): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(manifestUrl).build()
                val body = client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@use null
                    }
                    response.body?.string()
                } ?: return@withContext failed(
                    "Update manifest request failed (non-2xx response or empty body)."
                )

                val manifest = UpdateManifestParser.parse(body)
                UpdateAvailabilityChecker.check(installedVersionCode, manifest)
            } catch (e: UpdateManifestParseException) {
                failed("Update manifest body was malformed: ${e.message}")
            } catch (e: IOException) {
                failed("Could not reach the update server: ${e.message}")
            }
        }

    // El motivo detallado solo se loguea (en inglés, como el resto del código): la UI
    // siempre muestra un único mensaje genérico en español, así que estas cadenas no
    // llegan a la persona usuaria.
    private fun failed(reason: String): UpdateCheckResult {
        Log.w("UpdateCheck", reason)
        return UpdateCheckResult.Failed(reason)
    }
}
