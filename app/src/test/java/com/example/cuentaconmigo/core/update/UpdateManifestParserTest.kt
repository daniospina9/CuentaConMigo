package com.example.cuentaconmigo.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Test de [UpdateManifestParser] contra la implementación real de `org.json` en la JVM
 * (agregada como dependencia de test, ver `gradle/libs.versions.toml`), no el stub de
 * Android que trae el framework y lanza `RuntimeException("Stub!")`.
 */
class UpdateManifestParserTest {

    @Test
    fun `parses a well formed manifest`() {
        val json = """
            {
              "versionCode": 2,
              "versionName": "1.1",
              "apkUrl": "https://example.com/app.apk",
              "notes": "Bug fixes"
            }
        """.trimIndent()

        val manifest = UpdateManifestParser.parse(json)

        assertEquals(
            UpdateManifest(
                versionCode = 2,
                versionName = "1.1",
                apkUrl = "https://example.com/app.apk",
                notes = "Bug fixes"
            ),
            manifest
        )
    }

    @Test
    fun `empty notes is a valid manifest`() {
        val json = """
            {
              "versionCode": 2,
              "versionName": "1.1",
              "apkUrl": "https://example.com/app.apk",
              "notes": ""
            }
        """.trimIndent()

        val manifest = UpdateManifestParser.parse(json)

        assertEquals("", manifest.notes)
    }

    @Test
    fun `missing required field throws UpdateManifestParseException`() {
        val json = """
            {
              "versionName": "1.1",
              "apkUrl": "https://example.com/app.apk",
              "notes": ""
            }
        """.trimIndent()

        assertThrows(UpdateManifestParseException::class.java) {
            UpdateManifestParser.parse(json)
        }
    }

    @Test
    fun `wrong type for a field throws UpdateManifestParseException`() {
        val json = """
            {
              "versionCode": "not-a-number",
              "versionName": "1.1",
              "apkUrl": "https://example.com/app.apk",
              "notes": ""
            }
        """.trimIndent()

        assertThrows(UpdateManifestParseException::class.java) {
            UpdateManifestParser.parse(json)
        }
    }

    @Test
    fun `malformed json throws UpdateManifestParseException`() {
        val json = "not json at all"

        assertThrows(UpdateManifestParseException::class.java) {
            UpdateManifestParser.parse(json)
        }
    }

    @Test
    fun `http apkUrl is accepted`() {
        val json = """
            {
              "versionCode": 2,
              "versionName": "1.1",
              "apkUrl": "http://example.com/app.apk",
              "notes": ""
            }
        """.trimIndent()

        val manifest = UpdateManifestParser.parse(json)

        assertEquals("http://example.com/app.apk", manifest.apkUrl)
    }

    @Test
    fun `blank apkUrl throws UpdateManifestParseException`() {
        val json = """
            {
              "versionCode": 2,
              "versionName": "1.1",
              "apkUrl": "",
              "notes": ""
            }
        """.trimIndent()

        assertThrows(UpdateManifestParseException::class.java) {
            UpdateManifestParser.parse(json)
        }
    }

    @Test
    fun `apkUrl without http or https scheme throws UpdateManifestParseException`() {
        val json = """
            {
              "versionCode": 2,
              "versionName": "1.1",
              "apkUrl": "ftp://example.com/app.apk",
              "notes": ""
            }
        """.trimIndent()

        assertThrows(UpdateManifestParseException::class.java) {
            UpdateManifestParser.parse(json)
        }
    }
}
