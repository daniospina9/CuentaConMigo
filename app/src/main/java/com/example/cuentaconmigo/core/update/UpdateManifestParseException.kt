package com.example.cuentaconmigo.core.update

/** Lanzada por [UpdateManifestParser] cuando el cuerpo del manifiesto no tiene la forma esperada. */
class UpdateManifestParseException(message: String, cause: Throwable? = null) : Exception(message, cause)
