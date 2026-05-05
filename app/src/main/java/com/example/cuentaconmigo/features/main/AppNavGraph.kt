package com.example.cuentaconmigo.features.main

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cuentaconmigo.features.accounts.deposit.DepositAccountListScreen
import com.example.cuentaconmigo.features.accounts.destination.DestinationAccountListScreen
import com.example.cuentaconmigo.features.home.DepositAccountTransactionsScreen
import com.example.cuentaconmigo.features.investments.InvestmentDetailScreen
import com.example.cuentaconmigo.features.investments.InvestmentSubAccountDetailScreen
import com.example.cuentaconmigo.features.reports.AccountTransactionsScreen
import com.example.cuentaconmigo.features.reports.FinancialReportScreen
import com.example.cuentaconmigo.features.transactions.form.TransactionFormScreen
import com.example.cuentaconmigo.features.transactions.transfer.TransferScreen
import com.example.cuentaconmigo.features.transactions.voice.VoiceInputScreen
import com.example.cuentaconmigo.features.users.UserSelectorScreen

object Routes {
    const val USER_SELECTOR = "user_selector"
    const val HOME = "home/{userId}"
    const val DEPOSIT_ACCOUNTS = "deposit_accounts/{userId}"
    const val DESTINATION_ACCOUNTS = "destination_accounts/{userId}"
    const val TRANSACTION_FORM = "transaction_form/{userId}/{type}?transactionId={transactionId}"
    const val VOICE_INPUT = "voice_input/{userId}"
    const val TRANSFER = "transfer/{userId}"
    const val ACCOUNT_TRANSACTIONS =
        "account_transactions/{userId}/{destinationAccountId}/{startDay}/{endDay}?accountName={accountName}"
    const val FINANCIAL_REPORT = "financial_report/{userId}"
    const val INVESTMENT_DETAIL = "investment_detail/{userId}/{accountId}"
    const val INVESTMENT_SUB_ACCOUNT = "investment_sub_account/{userId}/{subAccountId}"
    const val DEPOSIT_ACCOUNT_TRANSACTIONS =
        "deposit_account_transactions/{userId}/{depositAccountId}?accountName={accountName}"

    fun home(userId: Long) = "home/$userId"
    fun depositAccounts(userId: Long) = "deposit_accounts/$userId"
    fun destinationAccounts(userId: Long) = "destination_accounts/$userId"
    fun transactionForm(userId: Long, type: String = "EXPENSE") = "transaction_form/$userId/$type"
    fun transactionFormEdit(userId: Long, transactionId: Long, type: String) =
        "transaction_form/$userId/$type?transactionId=$transactionId"
    fun voiceInput(userId: Long) = "voice_input/$userId"
    fun transfer(userId: Long) = "transfer/$userId"
    fun accountTransactions(userId: Long, destinationAccountId: Long, accountName: String, startDay: Long, endDay: Long) =
        "account_transactions/$userId/$destinationAccountId/$startDay/$endDay?accountName=${Uri.encode(accountName)}"
    fun financialReport(userId: Long) = "financial_report/$userId"
    fun investmentDetail(userId: Long, accountId: Long) = "investment_detail/$userId/$accountId"
    fun investmentSubAccount(userId: Long, subAccountId: Long) = "investment_sub_account/$userId/$subAccountId"
    fun depositAccountTransactions(userId: Long, depositAccountId: Long, accountName: String) =
        "deposit_account_transactions/$userId/$depositAccountId?accountName=${Uri.encode(accountName)}"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.USER_SELECTOR) {

        composable(Routes.USER_SELECTOR) {
            UserSelectorScreen(
                onUserSelected = { user ->
                    navController.navigate(Routes.home(user.id)) {
                        popUpTo(Routes.USER_SELECTOR) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStack ->
            val userId = backStack.arguments!!.getLong("userId")
            MainScreen(userId = userId, navController = navController)
        }

        composable(
            route = Routes.DEPOSIT_ACCOUNTS,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) {
            DepositAccountListScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.DESTINATION_ACCOUNTS,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) {
            DestinationAccountListScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.TRANSACTION_FORM,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) {
            TransactionFormScreen(
                onSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VOICE_INPUT,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStack ->
            val userId = backStack.arguments!!.getLong("userId")
            VoiceInputScreen(
                onNavigateToForm = { navController.navigate(Routes.transactionForm(userId)) },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TRANSFER,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) {
            TransferScreen(
                onSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ACCOUNT_TRANSACTIONS,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("destinationAccountId") { type = NavType.LongType },
                navArgument("startDay") { type = NavType.LongType },
                navArgument("endDay") { type = NavType.LongType },
                navArgument("accountName") { type = NavType.StringType; defaultValue = "Cuenta" }
            )
        ) { backStack ->
            val userId = backStack.arguments!!.getLong("userId")
            val accountName = backStack.arguments?.getString("accountName") ?: "Cuenta"
            AccountTransactionsScreen(
                accountName = accountName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { tx ->
                    navController.navigate(Routes.transactionFormEdit(userId, tx.id, tx.type.name))
                }
            )
        }

        composable(
            route = Routes.FINANCIAL_REPORT,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) {
            FinancialReportScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.DEPOSIT_ACCOUNT_TRANSACTIONS,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("depositAccountId") { type = NavType.LongType },
                navArgument("accountName") { type = NavType.StringType; defaultValue = "Cuenta" }
            )
        ) { backStack ->
            val userId = backStack.arguments!!.getLong("userId")
            val accountName = backStack.arguments?.getString("accountName") ?: "Cuenta"
            DepositAccountTransactionsScreen(
                accountName = accountName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { tx ->
                    navController.navigate(Routes.transactionFormEdit(userId, tx.id, tx.type.name))
                }
            )
        }

        composable(
            route = Routes.INVESTMENT_DETAIL,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("accountId") { type = NavType.LongType }
            )
        ) { backStack ->
            val userId = backStack.arguments!!.getLong("userId")
            InvestmentDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSubAccount = { subAccountId ->
                    navController.navigate(Routes.investmentSubAccount(userId, subAccountId))
                }
            )
        }

        composable(
            route = Routes.INVESTMENT_SUB_ACCOUNT,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("subAccountId") { type = NavType.LongType }
            )
        ) {
            InvestmentSubAccountDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
