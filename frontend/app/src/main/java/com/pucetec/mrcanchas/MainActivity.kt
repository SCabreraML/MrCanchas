package com.pucetec.mrcanchas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pucetec.mrcanchas.services.SessionManager
import com.pucetec.mrcanchas.ui.screens.courts.CourtDetailScreen
import com.pucetec.mrcanchas.ui.screens.courts.CourtsScreen
import com.pucetec.mrcanchas.ui.screens.home.HomeScreen
import com.pucetec.mrcanchas.ui.screens.login.LoginScreen
import com.pucetec.mrcanchas.ui.screens.match.MatchResultScreen
import com.pucetec.mrcanchas.ui.screens.reservations.ReservationDetailScreen
import com.pucetec.mrcanchas.ui.screens.reservations.ReservationsScreen
import com.pucetec.mrcanchas.ui.theme.MrCanchasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MrCanchasTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val startDestination = if (sessionManager.isLoggedIn()) "home" else "login"

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    onNavigateToCourts = { navController.navigate("courts") },
                    onNavigateToReservations = { navController.navigate("reservations") },
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("courts") {
                CourtsScreen(
                    onNavigateToDetail = { courtId ->
                        navController.navigate("courts/$courtId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "courts/{courtId}",
                arguments = listOf(navArgument("courtId") { type = NavType.LongType })
            ) { backStackEntry ->
                val courtId = backStackEntry.arguments?.getLong("courtId") ?: 0L
                CourtDetailScreen(
                    courtId = courtId,
                    onBack = { navController.popBackStack() },
                    onNavigateToMyReservations = {
                        navController.navigate("reservations") {
                            popUpTo("courts") { inclusive = true }
                        }
                    }
                )
            }
            composable("reservations") {
                ReservationsScreen(
                    onNavigateToDetail = { reservationId ->
                        navController.navigate("reservations/$reservationId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "reservations/{reservationId}",
                arguments = listOf(navArgument("reservationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getLong("reservationId") ?: 0L
                ReservationDetailScreen(
                    reservationId = reservationId,
                    onBack = { navController.popBackStack() },
                    onNavigateToMatchResult = { resId ->
                        navController.navigate("match_results/$resId")
                    }
                )
            }
            composable(
                route = "match_results/{reservationId}",
                arguments = listOf(navArgument("reservationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getLong("reservationId") ?: 0L
                MatchResultScreen(
                    reservationId = reservationId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
