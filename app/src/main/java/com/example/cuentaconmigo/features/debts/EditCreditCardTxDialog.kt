package com.example.cuentaconmigo.features.debts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.cuentaconmigo.core.util.filterAmountInput
import com.example.cuentaconmigo.core.util.parseToCentavos
import com.example.cuentaconmigo.core.util.toCopString
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.DepositAccount
import com.example.cuentaconmigo.domain.model.DestinationAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditCreditCardTxDialog(
    tx: CreditCardTransaction,
    destinationAccounts: List<DestinationAccount>,
    depositAccounts: List<DepositAccount>,
    tem: Double,
    onConfirm: (CreditCardTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    val initialAmountText = remember(tx.id) {
        filterAmountInput("", (tx.amount / 100).toString())
    }
    var amountTfv by remember(tx.id) {
        mutableStateOf(TextFieldValue(initialAmountText, TextRange(initialAmountText.length)))
    }
    var description by remember(tx.id) { mutableStateOf(tx.description ?: "") }
    var cuotasStr by remember(tx.id) { mutableStateOf(tx.installments.toString()) }
    var selectedDestAccount by remember(tx.id, destinationAccounts) {
        mutableStateOf(destinationAccounts.firstOrNull { it.id == tx.destinationAccountId })
    }
    var destMenuExpanded by remember { mutableStateOf(false) }
    // The category lives on the linked expense too, so it can be changed but never cleared.
    // A purchase without a linked expense has no category to keep in sync, so the selector is hidden.
    val categoryEditable = tx.type == CreditCardTransactionType.PURCHASE &&
        tx.linkedTransactionId != null &&
        destinationAccounts.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar movimiento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountTfv,
                    onValueChange = { new -> amountTfv = filterAmountInput(amountTfv, new) },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                when (tx.type) {
                    CreditCardTransactionType.PURCHASE -> {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cuotasStr,
                            onValueChange = { cuotasStr = it },
                            label = { Text("Número de cuotas (1 = de contado)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val cuotas = cuotasStr.toIntOrNull() ?: 1
                        val amountCentavos = amountTfv.text.parseToCentavos() ?: 0L
                        if (cuotas > 1 && amountCentavos > 0 && tem > 0) {
                            val cuotaMensual = amountCentavos * (tem * Math.pow(1 + tem, cuotas.toDouble())) / (Math.pow(1 + tem, cuotas.toDouble()) - 1)
                            Text(
                                "Cuota mensual aprox: ${cuotaMensual.toLong().toCopString()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (categoryEditable) {
                            ExposedDropdownMenuBox(
                                expanded = destMenuExpanded,
                                onExpandedChange = { destMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedDestAccount?.name ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoría") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destMenuExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = destMenuExpanded,
                                    onDismissRequest = { destMenuExpanded = false }
                                ) {
                                    destinationAccounts.forEach { acc ->
                                        DropdownMenuItem(
                                            text = { Text(acc.name) },
                                            onClick = { selectedDestAccount = acc; destMenuExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    CreditCardTransactionType.PAYMENT -> {
                        Text(
                            "El pago vinculado en la cuenta de depósito también se actualizará.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    CreditCardTransactionType.INTEREST,
                    CreditCardTransactionType.FEE -> {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            val newAmount = amountTfv.text.parseToCentavos() ?: 0L
            TextButton(
                onClick = {
                    val newCuotas = cuotasStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onConfirm(
                        tx.copy(
                            amount = newAmount,
                            description = description.ifBlank { null },
                            installments = newCuotas,
                            destinationAccountId = selectedDestAccount?.id
                                ?.takeIf { categoryEditable }
                                ?: tx.destinationAccountId
                        )
                    )
                },
                enabled = newAmount > 0
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
