package com.example.cuentaconmigo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// === Colores crudos de marca (no entran en el ColorScheme de Material) ===
val BannerGreenDark = Color(0xFF023505)
val BannerGreenLight = Color(0xFF127E29)
val AiCardMint = Color(0xFFF0F6EE)
val AccountChipGreenLight = Color(0xFF95F652)
val AccountChipGreenDark = Color(0xFF0C7D22)

// === La "caja" de tokens de marca: agrupa gradientes y colores propios ===
@Immutable
data class BrandColors(
    val bannerGradient: Brush,
    val aiMicGradient: Brush,
    val aiCardContainer: Color,
    val bannerOverlay: Color,     // overlay del dropdown sobre el banner
    val onBanner: Color,          // texto/íconos principales sobre el banner
    val onBannerVariant: Color,   // texto secundario sobre el banner
    val bannerDivider: Color,     // divisores sobre el banner
    val accountChipContainer: Brush, // fondo (gradiente translúcido) del chip de cuenta
    val accountChipIcon: Color,      // ícono del chip de cuenta de depósito
)

// === Instancia para tema CLARO ===
val LightBrand = BrandColors(
    bannerGradient = Brush.linearGradient(
        colors = listOf(BannerGreenDark, BannerGreenLight),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    ),
    aiMicGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF49E53C),
            0.05f to Color(0xFF40D738),
            0.10f to Color(0xFF36CA35),
            0.15f to Color(0xFF2DBB32),
            0.21f to Color(0xFF26AD2F),
            0.28f to Color(0xFF20A02D),
            0.35f to Color(0xFF1C982B),
            0.42f to Color(0xFF19922A),
            0.50f to Color(0xFF178F29),
            0.60f to Color(0xFF168D29),
            0.70f to Color(0xFF168C28),
            0.80f to Color(0xFF158B28),
            1.0f to Color(0xFF158B28)
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    ),
    aiCardContainer = AiCardMint,
    bannerOverlay = BannerGreenDark.copy(alpha = 0.3f),
    onBanner = Color.White,
    onBannerVariant = Color.White.copy(alpha = 0.7f),
    bannerDivider = Color.White.copy(alpha = 0.2f),
    accountChipContainer = Brush.linearGradient(
        colors = listOf(
            AccountChipGreenLight.copy(alpha = 0.50f),
            Color(0xFF3FA52B).copy(alpha = 0.42f)
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    ),
    accountChipIcon = AccountChipGreenDark,
)

// === Instancia para tema OSCURO (por ahora clona la clara; se afina luego) ===
val DarkBrand = LightBrand

// === El "cableado": deja la caja disponible en todo el árbol de composables ===
val LocalBrand = staticCompositionLocalOf { LightBrand }

// === Atajo de lectura, en simetría con MaterialTheme.colorScheme ===
val MaterialTheme.brand: BrandColors
    @Composable
    @ReadOnlyComposable
    get() = LocalBrand.current
