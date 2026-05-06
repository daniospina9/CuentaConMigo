package com.example.cuentaconmigo.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.features.main.Routes

@Composable
fun HomeContent(
    userId: Long,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (userName.isNotBlank()) {
            Text("Hola, $userName", style = MaterialTheme.typography.headlineSmall)
        }

        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Balance total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    totalBalance.toCopString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text("Cuentas de depósito", style = MaterialTheme.typography.labelLarge)

        if (accounts.isEmpty()) {
            Text(
                "Sin cuentas de depósito. Crea una en \"Gestionar cuentas\".",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            accounts.forEach { (account, balance) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Routes.depositAccountTransactions(userId, account.id, account.name)) }
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(account.name, style = MaterialTheme.typography.bodyLarge)
                        Text(balance.toCopString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        HorizontalDivider()
        Text("Nueva transacción", style = MaterialTheme.typography.labelLarge)

        Button(
            onClick = { navController.navigate(Routes.voiceInput(userId)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Por voz")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.transactionForm(userId, "EXPENSE")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gasto manual")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.transactionForm(userId, "INCOME")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingreso manual")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.transfer(userId)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Transferencia entre cuentas")
        }

        HorizontalDivider()
        Text("Gestionar cuentas", style = MaterialTheme.typography.labelLarge)

        OutlinedButton(
            onClick = { navController.navigate(Routes.depositAccounts(userId)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cuentas de depósito")
        }

        OutlinedButton(
            onClick = { navController.navigate(Routes.destinationAccounts(userId)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cuentas de destino / categorías")
        }

        Spacer(Modifier.height(80.dp))
    }
}
