package com.example.fleetflow.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.fleetflow.ui.Owner.OwnerViewModel
import com.example.fleetflow.ui.Driver.DriverViewModel

import com.example.fleetflow.ui.Welcome.OnboardingScreen
import com.example.fleetflow.ui.Welcome.SplashScreen

import com.example.fleetflow.ui.Owner.DriversManagementScreen
import com.example.fleetflow.ui.Owner.VehicleScreen
import com.example.fleetflow.ui.Owner.ReportsListScreen
import com.example.fleetflow.ui.Driver.ReportScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onLoadingFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    if (user.role == "owner") {
                        navController.navigate(Screen.OwnerDashboard.route + "/${user.id}") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.DriverDashboard.route + "/${user.id}") {
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
                onRegisterSuccess = { user ->
                    if (user.role == "owner") {
                        navController.navigate(Screen.OwnerDashboard.route + "/${user.id}") {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.DriverDashboard.route + "/${user.id}") {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.OwnerDashboard.route + "/{ownerId}") { backStackEntry ->
            val ownerId = backStackEntry.arguments?.getString("ownerId") ?: ""
            OwnerDashboardScreen(
                ownerId = ownerId,
                onNavigateToVehicles = { navController.navigate(Screen.Vehicles.route) },
                onNavigateToTrips = { navController.navigate(Screen.TripOverview.route) },
                onNavigateToMaintenance = { navController.navigate(Screen.Maintenance.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToDrivers = { id -> navController.navigate(Screen.Drivers.route + "/$id") },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
        composable(Screen.Reports.route) {
            ReportsListScreen()
        }

        // Driver Section
        composable(Screen.DriverDashboard.route + "/{driverId}") { backStackEntry ->
            val driverId = backStackEntry.arguments?.getString("driverId") ?: ""
            DriverDashboardScreen(
                driverId = driverId,
                onNavigateToRecordTrip = { navController.navigate(Screen.RecordTrip.route) },
                onNavigateToMyVehicle = { navController.navigate(Screen.MyVehicle.route) },
                onNavigateToReport = { navController.navigate(Screen.MakeReport.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.RecordTrip.route) {
            RecordTripScreen(
                onTripRecorded = { navController.popBackStack() }
            )
        }
        composable(Screen.MakeReport.route) {
            ReportScreen(
                onReportSubmitted = { navController.popBackStack() }
            )
        }
        composable(Screen.MyVehicle.route) {
            MyVehicleScreen()
        }
        composable(Screen.Drivers.route + "/{ownerId}") { backStackEntry ->
            val ownerId = backStackEntry.arguments?.getString("ownerId") ?: ""
            DriversManagementScreen(ownerId = ownerId)
        }
    }
}
