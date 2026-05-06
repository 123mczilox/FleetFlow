package com.example.fleetflow.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Owner
    object OwnerDashboard : Screen("owner_dashboard")
    object Vehicles : Screen("vehicles")
    object AddVehicle : Screen("add_vehicle")
    object TripOverview : Screen("trip_overview")
    object Maintenance : Screen("maintenance")
    
    // Driver
    object DriverDashboard : Screen("driver_dashboard")
    object RecordTrip : Screen("record_trip")
    object MyVehicle : Screen("my_vehicle")
}
