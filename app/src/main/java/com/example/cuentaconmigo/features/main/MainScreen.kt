package com.example.cuentaconmigo.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.cuentaconmigo.features.home.HomeContent
import com.example.cuentaconmigo.features.investments.InvestmentContent
import com.example.cuentaconmigo.features.reports.ReportsContent
import com.example.cuentaconmigo.features.savings.SavingsContent

enum class HomeTab(val label: String) {
    HOME("Inicio"),
    SAVINGS("Ahorros"),
    INVESTMENTS("Inversiones"),
    REPORTS("Reportes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userId: Long, navController: NavController) {
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (selectedTab != HomeTab.HOME) {
                    TopAppBar(title = { Text(selectedTab.label) })
                }
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(
                        top    = padding.calculateTopPadding()
                    )
            ) {
                when (selectedTab) {
                    HomeTab.HOME -> HomeContent(userId = userId, navController = navController)
                    HomeTab.SAVINGS -> SavingsContent(
                        onNavigateToDetail = { accountId ->
                            navController.navigate(Routes.savingsDetail(userId, accountId))
                        }
                    )
                    HomeTab.INVESTMENTS -> InvestmentContent(
                        onNavigateToDetail = { accountId ->
                            navController.navigate(Routes.investmentDetail(userId, accountId))
                        }
                    )
                    HomeTab.REPORTS -> ReportsContent(userId = userId, navController = navController)
                }
            }
        }

        NotchedBottomBar(
            modifier       = Modifier.align(Alignment.BottomCenter),
            selectedTab    = selectedTab,
            onTabSelected  = { selectedTab = it },
            onAddClick     = { navController.navigate(Routes.transactionForm(userId, "EXPENSE")) }
        )
    }
}