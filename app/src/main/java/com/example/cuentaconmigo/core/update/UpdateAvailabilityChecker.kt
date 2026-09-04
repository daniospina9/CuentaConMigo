package com.example.cuentaconmigo.core.update

/**
 * Comparación pura entre el `versionCode` instalado y el que reporta el manifiesto
 * remoto. No depende de Android ni de JSON, así que es testeable con JUnit puro.
 */
object UpdateAvailabilityChecker {

    /**
     * @param installedVersionCode típicamente `BuildConfig.VERSION_CODE`.
     * @param manifest manifiesto ya parseado con el `versionCode` remoto.
     * @return [UpdateCheckResult.UpdateAvailable] si el remoto es mayor al instalado,
     *   [UpdateCheckResult.UpToDate] si es igual o menor.
     */
    fun check(installedVersionCode: Int, manifest: UpdateManifest): UpdateCheckResult =
        if (manifest.versionCode > installedVersionCode) {
            UpdateCheckResult.UpdateAvailable(manifest)
        } else {
            UpdateCheckResult.UpToDate
        }
}
