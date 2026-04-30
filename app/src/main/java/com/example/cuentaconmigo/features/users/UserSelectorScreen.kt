package com.example.cuentaconmigo.features.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuentaconmigo.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectorScreen(
    onUserSelected: (User) -> Unit,
    viewModel: UserSelectorViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CuentaConMigo") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear usuario")
            }
        }
    ) { padding ->
        if (users.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay usuarios. Crea uno con el botón +")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(users) { user ->
                    ListItem(
                        headlineContent = { Text(user.name) },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.clickable { onUserSelected(user) }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showCreateDialog) {
            CreateUserDialog(
                onConfirm = { name ->
                    viewModel.createUser(name) { user ->
                        showCreateDialog = false
                        onUserSelected(user)
                    }
                },
                onDismiss = { showCreateDialog = false }
            )
        }

        errorMessage?.let { msg ->
            LaunchedEffect(msg) {
                viewModel.clearError()
            }
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
            ) { Text(msg) }
        }
    }
}

@Composable
private fun CreateUserDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo usuario") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}