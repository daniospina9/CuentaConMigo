package com.example.cuentaconmigo.core.update

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test JUnit puro (sin Android, sin Robolectric) de la comparación de `versionCode`
 * usada para decidir si hay una actualización disponible. [UpdateAvailabilityChecker]
 * no depende de Android ni de parseo de JSON, así que corre en la JVM plana.
 */
class UpdateAvailabilityCheckerTest {

    private fun manifest(versionCode: Int) = UpdateManifest(
        versionCode = versionCode,
        versionName = "1.$versionCode",
        apkUrl = "https://example.com/app.apk",
        notes = ""
    )

    @Test
    fun `remote versionCode lower than installed is UpToDate`() {
        val result = UpdateAvailabilityChecker.check(
            installedVersionCode = 10,
            manifest = manifest(versionCode = 9)
        )

        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun `remote versionCode equal to installed is UpToDate`() {
        val result = UpdateAvailabilityChecker.check(
            installedVersionCode = 10,
            manifest = manifest(versionCode = 10)
        )

        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun `remote versionCode higher than installed is UpdateAvailable`() {
        val remote = manifest(versionCode = 11)

        val result = UpdateAvailabilityChecker.check(installedVersionCode = 10, manifest = remote)

        assertEquals(UpdateCheckResult.UpdateAvailable(remote), result)
    }
}
