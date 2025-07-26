package com.example.pos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.pos.ui.history.HistoryScreen
import com.example.pos.ui.sale.CheckoutScreen
import com.example.pos.ui.sale.SaleScreen
import com.example.pos.ui.sale.SaleViewModel
import com.example.pos.ui.theme.PosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PosTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "sales_flow") {
                    navigation(
                        startDestination = "sale",
                        route = "sales_flow"
                    ) {
                        // レジ画面
                        composable("sale") { navBackStackEntry ->
                            // 親グラフに紐付いたViewModelを取得
                            val parentEntry = remember(navBackStackEntry) {
                                navController.getBackStackEntry("sales_flow")
                            }
                            val saleViewModel: SaleViewModel = hiltViewModel(parentEntry)

                            SaleScreen(
                                saleViewModel = saleViewModel, // 共有ViewModelを渡す
                                onNavigateToCheckout = { navController.navigate("checkout") },
                                onNavigateToHistory = { navController.navigate("history") }
                            )
                        }
                        // 会計画面
                        composable("checkout") { navBackStackEntry ->
                            // 親グラフに紐付いたViewModelを取得
                            val parentEntry = remember(navBackStackEntry) {
                                navController.getBackStackEntry("sales_flow")
                            }
                            val saleViewModel: SaleViewModel = hiltViewModel(parentEntry)

                            CheckoutScreen(
                                saleViewModel = saleViewModel, // 共有ViewModelを渡す
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("history") {
                            HistoryScreen()
                        }
                    }
                }
            }
        }
    }
}