package com.example.fleetflow.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Owner
    object OwnerDashboard : Screen("owner_dashboard")
    object Vehicles : Screen("vehicles")
    object AddVehicle : Screen("add_vehicle")
    object TripOverview : Screen("trip_overview")
    object Maintenance : Screen("maintenance")
    object Drivers : Screen("drivers")
    object Reports : Screen("reports")
    
    // Driver
    object DriverDashboard : Screen("driver_dashboard")
    object RecordTrip : Screen("record_trip")
    object MyVehicle : Screen("my_vehicle")
    object MakeReport : Screen("make_report")
}
