package com.example.fleetflow.Data.Service

import com.example.fleetflow.Data.Model.Report
import io.github.jan.supabase.postgrest.postgrest

class ReportService {
    private val client = SupabaseClient.client

    suspend fun createReport(report: Report) {
        client.postgrest["reports"].insert(report)
    }

    suspend fun getReportsByOwner(ownerId: String): List<Report> {
        return client.postgrest["reports"].select {
            filter {
                eq("owner_id", ownerId)
            }
        }.decodeList<Report>()
    }

    suspend fun getReportsByDriver(driverId: String): List<Report> {
        return client.postgrest["reports"].select {
            filter {
                eq("driver_id", driverId)
            }
        }.decodeList<Report>()
    }
}
