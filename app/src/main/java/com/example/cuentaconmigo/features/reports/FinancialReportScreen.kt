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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.AccountTotal
import com.example.cuentaconmigo.domain.model.DepositAccountStatement
import com.example.cuentaconmigo.domain.model.Transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancialReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val shortFmt = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }
    val txFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CO")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informe personalizado") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Rango de fechas", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerButton(
                        label = "Desde",
                        date = startDate,
                        formatter = shortFmt,
                        modifier = Modifier.weight(1f),
                        onDateSelected = { viewModel.setStartDate(it) }
                    )
                    DatePickerButton(
                        label = "Hasta",
                        date = endDate,
                        formatter = shortFmt,
                        modifier = Modifier.weight(1f),
                        onDateSelected = { viewModel.setEndDate(it) }
                    )
                }
                state.error?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    LaunchedEffect(err) { viewModel.clearError() }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { viewModel.generate() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text(if (state.isLoading) "Generando..." else "Generar informe")
                }
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (state.generated && !state.isLoading) {

                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Estado de resultados", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                }

                items(state.depositStatements) { stmt ->
                    StatementCard(stmt)
                }

                if (state.expenseByCategory.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("Gastos por categoría", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    }
                    items(state.expenseByCategory) { cat ->
                        CategoryRow(cat)
                        HorizontalDivider()
                    }
                    item {
                        val total = state.expenseByCategory.sumOf { it.total }
                        ListItem(
                            headlineContent = { Text("Total", style = MaterialTheme.typography.titleSmall) },
                            trailingContent = { Text(total.toCopString(), style = MaterialTheme.typography.titleSmall) }
                        )
                    }
                }

                if (state.transactions.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("Detalle de transacciones", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    }
                    items(state.transactions) { tx ->
                        TransactionListItem(tx, txFmt)
                        HorizontalDivider()
                    }
                } else {
                    item {
                        Text(
                            "Sin transacciones en el período seleccionado",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatementCard(stmt: DepositAccountStatement) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stmt.accountName, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            ReportRow("Saldo inicial", stmt.openingBalance.toCopString())
            ReportRow("+ Ingresos del período", stmt.periodIncome.toCopString())
            ReportRow("- Gastos del período", stmt.periodExpense.toCopString())
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            ReportRow("Saldo final", stmt.closingBalance.toCopString(), bold = true)
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, bold: Boolean = false) {
    val weight = if (bold) FontWeight.SemiBold else FontWeight.Normal
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = weight, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = weight, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CategoryRow(cat: AccountTotal) {
    ListItem(
        headlineContent = { Text(cat.destinationAccountName) },
        trailingContent = { Text(cat.total.toCopString()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerButton(
    label: String,
    date: LocalDate?,
    formatter: DateTimeFormatter,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { showPicker = true }, modifier = modifier) {
        Text(date?.format(formatter) ?: label)
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selected)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
