package com.example.cuentaconmigo.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuentaconmigo.R
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.features.main.Routes
import com.example.cuentaconmigo.ui.theme.Green40
import com.example.cuentaconmigo.ui.theme.brand

@Composable
fun HomeContent(
    userId: Long,
    navController: NavController,
    bottomPadding: Dp = 0.dp,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val income by viewModel.income.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var balanceVisible by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Saludo ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (userName.isNotBlank()) "Hola, $userName 👋" else "Hola 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Todo en orden, sigue por buen camino",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Banner verde ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.brand.bannerGradient)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp)) {
                // Label + dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Balance total",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.brand.onBannerVariant
                        )
                        Icon(
                            imageVector = if (balanceVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (balanceVisible) "Ocultar balance" else "Mostrar balance",
                            tint = MaterialTheme.brand.onBannerVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { balanceVisible = !balanceVisible }
                        )
                    }
                    PeriodDropdown(
                        selected = selectedPeriod,
                        onSelect = { viewModel.setPeriod(it) }
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Monto total
                Text(
                    text = if (balanceVisible) totalBalance.toCopString() else "••••••",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.brand.onBanner
                )

                Spacer(Modifier.height(16.dp))

                // Divider
                HorizontalDivider(color = MaterialTheme.brand.bannerDivider)

                Spacer(Modifier.height(14.dp))

                // Ingresos | Gastos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ingresos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.brand.onBannerVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (balanceVisible) income.toCopString() else "••••••",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.brand.onBanner
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.height(36.dp),
                        color = MaterialTheme.brand.bannerDivider
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Gastos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.brand.onBannerVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (balanceVisible) expenses.toCopString() else "••••••",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.brand.onBanner
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Card IA ───────────────────────────────────────────────────────────
        AiRegistrationCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            onMicClick = { navController.navigate(Routes.voiceInput(userId)) }
        )

        Spacer(Modifier.height(24.dp))

        // ── Cuentas de depósito ───────────────────────────────────────────────
        SectionLabel(
            text = "Cuentas de depósito",
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(10.dp))

        if (accounts.isEmpty()) {
            Text(
                text = "Sin cuentas de depósito. Crea una en Gestionar.",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                accounts.forEach { (account, balance) ->
                    DepositAccountRow(
                        name = account.name,
                        balance = balance,
                        onClick = {
                            navController.navigate(
                                Routes.depositAccountTransactions(userId, account.id, account.name)
                            )
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Gestionar ─────────────────────────────────────────────────────────
        SectionLabel(
            text = "Gestionar",
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val tileBrush = MaterialTheme.brand.addButtonGradient
            ManageTile(
                icon = ImageVector.vectorResource(R.drawable.deposit_accounts),
                label = "Cuentas de depósito",
                iconBrush = tileBrush,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { navController.navigate(Routes.depositAccounts(userId)) }
            )
            ManageTile(
                icon = ImageVector.vectorResource(R.drawable.target_accounts),
                label = "Cuentas de destino",
                iconBrush = tileBrush,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { navController.navigate(Routes.destinationAccounts(userId)) }
            )
            ManageTile(
                icon = ImageVector.vectorResource(R.drawable.general_wallet),
                label = "Deudas y préstamos",
                iconBrush = tileBrush,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { navController.navigate(Routes.debtHub(userId)) }
            )
        }

        // Espacio para que la barra flotante no tape el último contenido.
        Spacer(Modifier.height(bottomPadding))
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

@Composable
private fun AiRegistrationCard(
    modifier: Modifier = Modifier,
    onMicClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.brand.aiCardContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 3.dp, end = 16.dp, bottom = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Contenido izquierdo
            Column(
                modifier = Modifier.weight(0.65f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Registro inteligente con IA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    modifier = Modifier.padding(start = 40.dp),
                    text = "Habla y deja que la IA registre por ti.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(0.08f))

            // Botón micrófono
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .wrapContentSize(Alignment.Center)
                    .size(70.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.brand.aiMicGradient)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = onMicClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Registrar por voz",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.weight(0.02f))
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
private fun PeriodDropdown(selected: HomePeriod, onSelect: (HomePeriod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.brand.bannerOverlay)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (selected == HomePeriod.MONTH) "Este mes" else "Anual",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Este mes") },
                onClick = { onSelect(HomePeriod.MONTH); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Anual") },
                onClick = { onSelect(HomePeriod.YEAR); expanded = false }
            )
        }
    }
}

@Composable
private fun DepositAccountRow(
    name: String,
    balance: Long,
    onClick: () -> Unit
) {
    val balanceColor = if (balance >= 0) Green40 else MaterialTheme.colorScheme.error

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip cuadrado con ícono — mismo para todas las cuentas de depósito
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.brand.accountChipContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.deposit_wallet),
                        contentDescription = null,
                        tint = MaterialTheme.brand.accountChipIcon,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Cuenta de depósito",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = balance.toCopString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = balanceColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ManageTile(
    icon: ImageVector,
    label: String,
    iconBrush: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Ícono grande tintado con el gradiente del botón "+"
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithContent {
                        drawContent()
                        drawRect(brush = iconBrush, blendMode = BlendMode.SrcAtop)
                    }
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}