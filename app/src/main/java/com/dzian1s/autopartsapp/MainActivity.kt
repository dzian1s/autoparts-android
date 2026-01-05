package com.dzian1s.autopartsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.dzian1s.autopartsapp.ui.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.dzian1s.autopartsapp.data.Api

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val nav = rememberNavController()
            val cart = remember { CartState() }

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
                    OrdersScreen(onBack = { nav.popBackStack() })
                }
            }
        }
    }
}
