package com.example.cuentaconmigo.features.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Cuánto sobresale el FAB sobre la barra — usado en MainScreen para ajustar padding
val NotchedBarFabOverhang = 28.dp

private val FabSize      = 56.dp
private val BarHeight    = 80.dp
private val NotchGap     = 10.dp
private val SideMargin   = 12.dp
private val BottomMargin = 10.dp
private val CornerRadius   = 36.dp   // esquinas muy redondeadas, casi píldora
private val NotchReach     = 20.dp   // radio del hombro (cuarto de círculo de entrada/salida)

@Composable
fun NotchedBottomBar(
    selectedTab:   HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onAddClick:    () -> Unit
) {
    val fabOverhang  = NotchedBarFabOverhang
    val sysNavBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Altura total medida por el Scaffold (incluye margen inferior + sys nav)
    val totalHeight   = BarHeight + fabOverhang + sysNavBottom + BottomMargin
    val barColor      = Color.White

    // El Box externo ocupa la pantalla completa en ancho para centrar bien el FAB,
    // pero el contenido visual (barra + ítems) tiene márgenes horizontales.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        // ── Fondo flotante: rect redondeado + hueco central ───────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SideMargin)
                .height(BarHeight)
                .align(Alignment.BottomCenter)
                .offset(y = -(sysNavBottom + BottomMargin))
        ) {
            val r  = (FabSize / 2 + NotchGap).toPx()   // radio del hueco
            val cr = CornerRadius.toPx()                // radio de las esquinas
            val rs = NotchReach.toPx()                  // alcance lateral de la curva de entrada/salida
            val k  = 0.5523f                            // constante bezier para círculo (90°)
            val k2 = 1.06f                               // controla apertura del fondo: mayor = más circular
            val w  = size.width
            val h  = size.height
            val cx = w / 2f

            val path = Path().apply {
                // Empezar en la esquina superior izquierda (tras el arco)
                moveTo(0f, cr)

                // ── Esquina sup-izq ─────────────────────────────────────
                cubicTo(0f, cr * (1-k),   cr * (1-k), 0f,   cr, 0f)

                // ── Borde superior izquierdo → inicio de la curva ────────
                lineTo(cx - r - rs, 0f)

                // ── Hueco: dos cúbicas continuas, sin junta visible ───────
                // k2 en P2/P1 preserva la curvatura circular en el fondo del hueco
                cubicTo(cx - r + 25, 0f,   cx - r * k2, r,   cx, r)
                cubicTo(cx + r * k2, r,   cx + r - 25, 0f,   cx + r + rs, 0f)

                // ── Borde superior derecho ────────────────────────────────
                lineTo(w - cr, 0f)

                // ── Esquina sup-der ───────────────────────────────────────
                cubicTo(w - cr * (1-k), 0f,   w, cr * (1-k),   w, cr)

                // ── Lado derecho ──────────────────────────────────────────
                lineTo(w, h - cr)

                // ── Esquina inf-der ───────────────────────────────────────
                cubicTo(w, h - cr * (1-k),   w - cr * (1-k), h,   w - cr, h)

                // ── Borde inferior ────────────────────────────────────────
                lineTo(cr, h)

                // ── Esquina inf-izq ───────────────────────────────────────
                cubicTo(cr * (1-k), h,   0f, h - cr * (1-k),   0f, h - cr)

                // ── Lado izquierdo (cierra hacia arriba) ──────────────────
                close()
            }

            // Sombra uniforme tipo card (irradia en todos los lados como elevation=2dp)
            translate(top = 4f,  left =  0f) { drawPath(path, Color.Black.copy(alpha = 0.05f)) }
            translate(top = 2f,  left =  2f) { drawPath(path, Color.Black.copy(alpha = 0.03f)) }
            translate(top = 2f,  left = -2f) { drawPath(path, Color.Black.copy(alpha = 0.03f)) }
            translate(top = 0f,  left =  2f) { drawPath(path, Color.Black.copy(alpha = 0.02f)) }
            translate(top = 0f,  left = -2f) { drawPath(path, Color.Black.copy(alpha = 0.02f)) }
            translate(top = -1f, left =  0f) { drawPath(path, Color.Black.copy(alpha = 0.03f)) }

            // Relleno de la barra
            drawPath(path, barColor)

            // Borde finísimo que define el contorno (igual al outline de una Card)
            drawPath(path, Color.Black.copy(alpha = 0.06f), style = Stroke(width = 0.8.dp.toPx()))
        }

        // ── Ítems de navegación (2 izq + espacio central + 2 der) ────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SideMargin)
                .height(BarHeight)
                .align(Alignment.BottomCenter)
                .offset(y = -(sysNavBottom + BottomMargin)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                modifier  = Modifier.weight(1f),
                selected  = selectedTab == HomeTab.HOME,
                icon      = Icons.Default.Home,
                label     = "Inicio",
                onClick   = { onTabSelected(HomeTab.HOME) }
            )
            BottomNavItem(
                modifier  = Modifier.weight(1f),
                selected  = selectedTab == HomeTab.SAVINGS,
                icon      = Icons.Default.Savings,
                label     = "Ahorros",
                onClick   = { onTabSelected(HomeTab.SAVINGS) }
            )
            // Espacio para el FAB
            Spacer(modifier = Modifier.weight(1f))
            BottomNavItem(
                modifier  = Modifier.weight(1f),
                selected  = selectedTab == HomeTab.INVESTMENTS,
                icon      = Icons.AutoMirrored.Filled.TrendingUp,
                label     = "Inversiones",
                onClick   = { onTabSelected(HomeTab.INVESTMENTS) }
            )
            BottomNavItem(
                modifier  = Modifier.weight(1f),
                selected  = selectedTab == HomeTab.REPORTS,
                icon      = Icons.Default.BarChart,
                label     = "Reportes",
                onClick   = { onTabSelected(HomeTab.REPORTS) }
            )
        }

        // ── FAB centrado — sobresale por encima de la barra ───────────────
        FloatingActionButton(
            onClick        = onAddClick,
            modifier       = Modifier
                .size(FabSize)
                .align(Alignment.TopCenter),
            shape          = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = Color.White,
            elevation      = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = "Nueva transacción",
                modifier           = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    modifier:  Modifier    = Modifier,
    selected:  Boolean,
    icon:      ImageVector,
    label:     String,
    onClick:   () -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary
               else          MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = tint,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}