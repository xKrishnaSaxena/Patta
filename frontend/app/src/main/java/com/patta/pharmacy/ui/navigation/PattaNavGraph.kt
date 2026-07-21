package com.patta.pharmacy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.patta.pharmacy.ui.screens.billing.BillingScreen
import com.patta.pharmacy.ui.screens.expiry.ExpiryScreen
import com.patta.pharmacy.ui.screens.home.HomeScreen
import com.patta.pharmacy.ui.screens.khata.CustomerLedgerScreen
import com.patta.pharmacy.ui.screens.khata.CustomersScreen
import com.patta.pharmacy.ui.screens.reports.ReportsScreen
import com.patta.pharmacy.ui.screens.medicine.AddEditMedicineScreen
import com.patta.pharmacy.ui.screens.placeholder.SimplePlaceholder
import com.patta.pharmacy.ui.screens.missedsale.MissedSalesScreen
import com.patta.pharmacy.ui.screens.more.MoreScreen
import com.patta.pharmacy.ui.screens.po.PurchaseOrderScreen
import com.patta.pharmacy.ui.screens.purchase.PurchaseEntryScreen
import com.patta.pharmacy.ui.screens.salereturn.BillReturnScreen
import com.patta.pharmacy.ui.screens.salereturn.RecentBillsScreen
import com.patta.pharmacy.ui.screens.gst.GstSummaryScreen
import com.patta.pharmacy.ui.screens.h1.ScheduleH1Screen
import com.patta.pharmacy.ui.screens.settings.ShopProfileScreen
import com.patta.pharmacy.ui.screens.settings.VoiceLanguageScreen
import com.patta.pharmacy.ui.screens.stock.StockListScreen
import com.patta.pharmacy.ui.screens.supplier.SupplierLedgerScreen
import com.patta.pharmacy.ui.screens.supplier.SuppliersScreen
import com.patta.pharmacy.ui.screens.voice.VoiceAssistantScreen

sealed class TopDest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : TopDest("home", "Home", Icons.Filled.Home)
    data object Billing : TopDest("billing", "Billing", Icons.AutoMirrored.Filled.ReceiptLong)
    data object Stock : TopDest("stock", "Stock", Icons.Filled.Inventory2)
    data object Khata : TopDest("khata", "Khata", Icons.AutoMirrored.Filled.MenuBook)
    data object More : TopDest("more", "More", Icons.Filled.MoreHoriz)
}

val bottomTabs = listOf(TopDest.Home, TopDest.Billing, TopDest.Stock, TopDest.Khata, TopDest.More)

object Routes {
    const val ADD_MEDICINE = "add_medicine"
    fun editMedicine(id: String) = "add_medicine?id=$id"
    const val SUPPLIERS = "suppliers"
    fun supplierLedger(id: String) = "supplier_ledger/$id"
    fun purchaseEntry(supplierId: String) = "purchase_entry/$supplierId"
    fun customerLedger(id: String) = "customer_ledger/$id"
}

@Composable
fun PattaNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PattaBottomBar(
                    currentDestination = backStackEntry?.destination,
                    onSelect = { dest ->
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopDest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(TopDest.Home.route) {
                HomeScreen(onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(TopDest.Billing.route) { BillingScreen() }
            composable(TopDest.Stock.route) {
                StockListScreen(
                    onAddMedicine = { navController.navigate(Routes.ADD_MEDICINE) },
                    onEditMedicine = { id -> navController.navigate(Routes.editMedicine(id)) },
                )
            }
            composable(TopDest.Khata.route) {
                CustomersScreen(onOpenCustomer = { id -> navController.navigate(Routes.customerLedger(id)) })
            }
            composable(TopDest.More.route) {
                MoreScreen(onNavigate = { route -> navController.navigate(route) })
            }

            composable(
                route = "add_medicine?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                }),
            ) {
                AddEditMedicineScreen(onDone = { navController.popBackStack() })
            }

            composable(Routes.SUPPLIERS) {
                SuppliersScreen(
                    onBack = { navController.popBackStack() },
                    onOpenSupplier = { id -> navController.navigate(Routes.supplierLedger(id)) },
                )
            }

            composable(
                route = "supplier_ledger/{supplierId}",
                arguments = listOf(navArgument("supplierId") { type = NavType.StringType }),
            ) {
                SupplierLedgerScreen(
                    onBack = { navController.popBackStack() },
                    onNewPurchase = { id -> navController.navigate(Routes.purchaseEntry(id)) },
                )
            }

            composable(
                route = "purchase_entry/{supplierId}",
                arguments = listOf(navArgument("supplierId") { type = NavType.StringType }),
            ) {
                PurchaseEntryScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = "customer_ledger/{customerId}",
                arguments = listOf(navArgument("customerId") { type = NavType.StringType }),
            ) {
                CustomerLedgerScreen(onBack = { navController.popBackStack() })
            }

            composable("expiry") { ExpiryScreen(onBack = { navController.popBackStack() }) }
            composable("reports") { ReportsScreen(onBack = { navController.popBackStack() }) }
            composable("voice_assistant") { VoiceAssistantScreen(onBack = { navController.popBackStack() }) }
            composable("voice_language") { VoiceLanguageScreen(onBack = { navController.popBackStack() }) }

            composable("missed_sales") {
                MissedSalesScreen(
                    onBack = { navController.popBackStack() },
                    onCreatePo = { navController.navigate("purchase_order") },
                )
            }
            composable("purchase_order") { PurchaseOrderScreen(onBack = { navController.popBackStack() }) }
            composable("shop_profile") { ShopProfileScreen(onBack = { navController.popBackStack() }) }
            composable("gst_summary") { GstSummaryScreen(onBack = { navController.popBackStack() }) }
            composable("h1_register") { ScheduleH1Screen(onBack = { navController.popBackStack() }) }

            composable("sale_return") {
                RecentBillsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenBill = { id -> navController.navigate("bill_return/$id") },
                )
            }
            composable(
                route = "bill_return/{billId}",
                arguments = listOf(navArgument("billId") { type = NavType.StringType }),
            ) {
                BillReturnScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun PattaBottomBar(
    currentDestination: NavDestination?,
    onSelect: (TopDest) -> Unit,
) {
    NavigationBar {
        bottomTabs.forEach { dest ->
            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(dest) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) },
            )
        }
    }
}
