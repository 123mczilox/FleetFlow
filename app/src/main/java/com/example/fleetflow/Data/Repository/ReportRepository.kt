package com.example.fleetflow.Data.Repository

import com.example.fleetflow.Data.Model.Report
import com.example.fleetflow.Data.Service.ReportService

class ReportRepository(private val reportService: ReportService) {
    suspend fun createReport(report: Report) = reportService.createReport(report)
    suspend fun getReportsByOwner(ownerId: String) = reportService.getReportsByOwner(ownerId)
    suspend fun getReportsByDriver(driverId: String) = reportService.getReportsByDriver(driverId)
}
