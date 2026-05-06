package com.example.fleetflow.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fleetflow.ui.Auth.LoginScreen
import com.example.fleetflow.ui.Auth.RegisterScreen
import com.example.fleetflow.ui.Driver.DriverDashboardScreen
import com.example.fleetflow.ui.Driver.MyVehicleScreen
import com.example.fleetflow.ui.Driver.RecordTripScreen
import com.example.fleetflow.ui.Owner.AddVehicleScreen
import com.example.fleetflow.ui.Owner.OwnerDashboardScreen
import com.example.fleetflow.ui.Owner.TripOverviewScreen
import com.example.fleetflow.ui.Owner.MaintenanceScreen
import com.example.fleetflow.ui.Owner.VehicleScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    if (role == "owner") {
                        navController.navigate(Screen.OwnerDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.DriverDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Owner Section
        composable(Screen.OwnerDashboard.route) {
            OwnerDashboardScreen(
                onNavigateToVehicles = { navController.navigate(Screen.Vehicles.route) },
                onNavigateToTrips = { navController.navigate(Screen.TripOverview.route) },
                onNavigateToMaintenance = { navController.navigate(Screen.Maintenance.route) }
            )
        }
        composable(Screen.Vehicles.route) {
            VehicleScreen(
                onNavigateToAddVehicle = { navController.navigate(Screen.AddVehicle.route) }
            )
        }
        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(
                onVehicleAdded = { navController.popBackStack() }
            )
        }
        composable(Screen.TripOverview.route) {
            TripOverviewScreen()
        }
        composable(Screen.Maintenance.route) {
            MaintenanceScreen()
        }

        // Driver Section
        composable(Screen.DriverDashboard.route) {
            DriverDashboardScreen(
                onNavigateToRecordTrip = { navController.navigate(Screen.RecordTrip.route) },
                onNavigateToMyVehicle = { navController.navigate(Screen.MyVehicle.route) }
            )
        }
        composable(Screen.RecordTrip.route) {
            RecordTripScreen(
                onTripRecorded = { navController.popBackStack() }
            )
        }
        composable(Screen.MyVehicle.route) {
            MyVehicleScreen()
        }
    }
}
