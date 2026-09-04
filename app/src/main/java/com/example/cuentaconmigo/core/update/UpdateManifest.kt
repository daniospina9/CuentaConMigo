package com.example.cuentaconmigo.core.update

/**
 * Manifiesto remoto de versión, servido en una URL fija y versionada (ver
 * [com.example.cuentaconmigo.BuildConfig.UPDATE_MANIFEST_URL]). Los cuatro campos
 * siempre están presentes en el JSON servido; [notes] es el único que puede venir vacío.
 */
data class UpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val notes: String
)
