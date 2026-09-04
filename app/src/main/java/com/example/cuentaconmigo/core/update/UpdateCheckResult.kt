package com.example.cuentaconmigo.core.update

/** Resultado de comprobar si hay una versión más nueva de la app disponible. */
sealed interface UpdateCheckResult {
    /** El `versionCode` de [manifest] es mayor al `versionCode` instalado. */
    data class UpdateAvailable(val manifest: UpdateManifest) : UpdateCheckResult

    /** El `versionCode` instalado ya es el más nuevo que reporta el manifiesto. */
    data object UpToDate : UpdateCheckResult

    /** El chequeo no pudo completarse: sin red, respuesta no-2xx, o cuerpo malformado. */
    data class Failed(val reason: String) : UpdateCheckResult
}
