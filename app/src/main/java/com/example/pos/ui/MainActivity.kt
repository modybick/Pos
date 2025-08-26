package com.example.pos.ui

// 👇 アニメーション用のimportを追加
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.pos.ui.history.HistoryScreen
import com.example.pos.ui.sale.CheckoutScreen
import com.example.pos.ui.sale.SaleScreen
import com.example.pos.ui.sale.SaleViewModel
import com.example.pos.ui.settings.SettingsScreen
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

                // 👇 ここでアニメーションの定義をまとめて作成する
                val animationSpec = tween<IntOffset>(300)
                val enterTransition =
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec)
                val exitTransition =
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec)
                val popEnterTransition =
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec)
                val popExitTransition =
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec)

                NavHost(navController = navController, startDestination = "sales_flow") {
                    navigation(
                        startDestination = "sale",
                        route = "sales_flow"
                    ) {
                        // レジ画面
                        composable(
                            "sale",
                            enterTransition = { enterTransition },
                            exitTransition = { exitTransition },
                            popEnterTransition = { popEnterTransition },
                            popExitTransition = { popExitTransition }
                        ) { navBackStackEntry ->
                            // 親グラフに紐付いたViewModelを取得
                            val parentEntry = remember(navBackStackEntry) {
                                navController.getBackStackEntry("sales_flow")
                            }
                            val saleViewModel: SaleViewModel = hiltViewModel(parentEntry)

                            // 👇 この画面が表示されるたびにリクエストをチェック
                            LaunchedEffect(Unit) {
                                saleViewModel.checkForCartReproductionRequest()
                            }

                            SaleScreen(
                                saleViewModel = saleViewModel, // 共有ViewModelを渡す
                                onNavigateToCheckout = { navController.navigate("checkout") },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        // 会計画面
                        composable(
                            "checkout",
                            enterTransition = { enterTransition },
                            exitTransition = { exitTransition },
                            popEnterTransition = { popEnterTransition },
                            popExitTransition = { popExitTransition }
                        ) { navBackStackEntry ->
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
                        composable(
                            "history",
                            enterTransition = { enterTransition },
                            exitTransition = { exitTransition },
                            popEnterTransition = { popEnterTransition },
                            popExitTransition = { popExitTransition }
                        ) {
                            HistoryScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "settings",
                            enterTransition = { enterTransition },
                            exitTransition = { exitTransition },
                            popEnterTransition = { popEnterTransition },
                            popExitTransition = { popExitTransition }
                        ) {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}