package com.example.cuentaconmigo.features.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cuentaconmigo.features.accounts.deposit.DepositAccountListScreen
import com.example.cuentaconmigo.features.accounts.destination.DestinationAccountListScreen
import com.example.cuentaconmigo.features.transactions.form.TransactionFormScreen
import com.example.cuentaconmigo.features.transactions.voice.VoiceInputScreen
import com.example.cuentaconmigo.features.users.UserSelectorScreen

object Routes {
    const val USER_SELECTOR = "user_selector"
    const val HOME = "home/{userId}"
    const val DEPOSIT_ACCOUNTS = "deposit_accounts/{userId}"
    const val DESTINATION_ACCOUNTS = "destination_accounts/{userId}"
    const val TRANSACTION_FORM = "transaction_form/{userId}/{type}"
    const val VOICE_INPUT = "voice_input/{userId}"

    fun home(userId: Long) = "home/$userId"
    fun depositAccounts(userId: Long) = "deposit_accounts/$userId"
    fun destinationAccounts(userId: Long) = "destination_accounts/$userId"
    fun transactionForm(userId: Long, type: String = "EXPENSE") = "transaction_form/$userId/$type"
    fun voiceInput(userId: Long) = "voice_input/$userId"
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
                navArgument("type") { type = NavType.StringType }
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
    }
}
