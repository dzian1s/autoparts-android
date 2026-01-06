package com.dzian1s.autopartsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.dzian1s.autopartsapp.ui.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dzian1s.autopartsapp.ui.theme.AutopartsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val cart = remember { CartState() }
            AutopartsTheme {
            NavHost(navController = nav, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        cart = cart,
                        onOpenCatalog = { nav.navigate("catalog") },
                        onOpenSearch = { nav.navigate("search") },
                        onOpenCart = { nav.navigate("cart") },
                        onOpenOrders = { nav.navigate("orders") }
                    )
                }
                composable("catalog") {
                    val vm: CatalogViewModel = viewModel()
                    CatalogScreen(
                        vm = vm,
                        onOpenSearch = { nav.navigate("search") },
                        onOpenDetails = { id -> nav.navigate("details/$id") }
                    )
                }
                composable("search") {
                    val vm: SearchViewModel = viewModel()
                    SearchScreen(
                        vm = vm,
                        onBack = { nav.popBackStack() },
                        onOpenDetails = { id -> nav.navigate("details/$id") }
                    )
                }
                composable(
                    route = "details/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    val id = entry.arguments?.getString("id")!!
                    val vm: DetailsViewModel = viewModel()
                    DetailsScreen(
                        id = id,
                        vm = vm,
                        cart = cart,
                        onBack = { nav.popBackStack() },
                        onOpenCart = { nav.navigate("cart") }
                    )
                }

                composable("cart") {
                    CartScreen(
                        cart = cart,
                        onBack = { nav.popBackStack() },
                        onOpenPolicy = { nav.navigate("privacy") }
                    )
                }
                composable("privacy") {
                    PrivacyPolicyScreen(onBack = { nav.popBackStack() })
                }
                composable("orders") {
                    val vm: OrdersViewModel = viewModel()
                    OrdersScreen(
                        vm = vm,
                        onBack = { nav.popBackStack() },
                        onOpenDetails = { id -> nav.navigate("orderDetails/$id") }
                    )
                }
                composable(
                    route = "orderDetails/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    val id = entry.arguments?.getString("id")!!
                    OrderDetailsScreen(orderId = id, onBack = { nav.popBackStack() })
                }
            }
            }
        }
    }
}
