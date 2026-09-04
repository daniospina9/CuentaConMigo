package com.example.cuentaconmigo.core.update

import org.json.JSONException
import org.json.JSONObject

/**
 * Parseo puro del manifiesto remoto de versión (`String` -> [UpdateManifest]). No
 * depende de Android, solo de `org.json`, así que es testeable en la JVM agregando
 * la dependencia de test `org.json:json` (ver `gradle/libs.versions.toml`).
 */
object UpdateManifestParser {

    /**
     * @throws UpdateManifestParseException si [json] no es JSON válido, le falta algún
     *   campo requerido, alguno tiene un tipo inesperado, o `apkUrl` no es una URL
     *   http(s) válida. `notes` es la única excepción: si falta, se toma como cadena
     *   vacía.
     */
    fun parse(json: String): UpdateManifest {
        try {
            val obj = JSONObject(json)
            val apkUrl = obj.getString("apkUrl")
            if (!isHttpUrl(apkUrl)) {
                throw UpdateManifestParseException("apkUrl no es una URL http(s) válida: $apkUrl")
            }
            return UpdateManifest(
                versionCode = obj.getInt("versionCode"),
                versionName = obj.getString("versionName"),
                apkUrl = apkUrl,
                notes = obj.optString("notes", "")
            )
        } catch (e: JSONException) {
            throw UpdateManifestParseException("Manifiesto de actualización malformado: ${e.message}", e)
        }
    }

    // apkUrl viene de un servidor remoto: se valida acá (no solo se confía en que sea
    // un string) para que capas de abajo (p. ej. abrir el navegador) nunca reciban un
    // valor que no sea un http(s) URL utilizable.
    private fun isHttpUrl(value: String): Boolean =
        value.isNotBlank() && (value.startsWith("http://") || value.startsWith("https://"))
}
