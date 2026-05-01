package com.example.cuentaconmigo.features.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransactionsScreen(
    accountName: String,
    onNavigateBack: () -> Unit,
    viewModel: AccountTransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CO")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(accountName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin movimientos en el período seleccionado")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(transactions) { tx ->
                    TransactionListItem(tx, formatter)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
internal fun TransactionListItem(tx: Transaction, formatter: DateTimeFormatter) {
    val amountColor = if (tx.type == TransactionType.INCOME)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error
    ListItem(
        headlineContent = { Text(tx.description ?: "Sin descripción") },
        supportingContent = { Text(tx.date.format(formatter)) },
        trailingContent = {
            Text(
                tx.amount.toCopString(),
                color = amountColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}
