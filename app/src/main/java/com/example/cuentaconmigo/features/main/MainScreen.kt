package com.example.cuentaconmigo.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.cuentaconmigo.features.home.HomeContent
import com.example.cuentaconmigo.features.investments.InvestmentContent
import com.example.cuentaconmigo.features.reports.ReportsContent

private enum class HomeTab(val label: String) {
    HOME("Inicio"),
    INVESTMENTS("Inversiones"),
    REPORTS("Reportes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userId: Long, navController: NavController) {
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(selectedTab.label) })
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            when (tab) {
                                HomeTab.HOME -> Icon(Icons.Default.Home, contentDescription = tab.label)
                                HomeTab.INVESTMENTS -> Icon(Icons.Default.TrendingUp, contentDescription = tab.label)
                                HomeTab.REPORTS -> Icon(Icons.Default.BarChart, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                HomeTab.HOME -> HomeContent(userId = userId, navController = navController)
                HomeTab.INVESTMENTS -> InvestmentContent(
                    onNavigateToDetail = { accountId ->
                        navController.navigate(Routes.investmentDetail(userId, accountId))
                    }
                )
                HomeTab.REPORTS -> ReportsContent(userId = userId, navController = navController)
            }
        }
    }
}
