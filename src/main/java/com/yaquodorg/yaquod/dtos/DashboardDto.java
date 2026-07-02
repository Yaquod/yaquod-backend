package com.yaquodorg.yaquod.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private long totalUsers;
    private long totalAdmins;
    private long totalClients;
    private long totalVehicles;
    private long idleVehicles;
    private long busyVehicles;
    private long unavailableVehicles;
    private long totalTrips;
    private long activeTrips;
    private long preTripTrips;
    private long completedTrips;
    private long cancelledTrips;
    private long issueTrips;
    private long pendingRequests;
    private long acceptedRequests;
    private long completedRequests;
    private long failedRequests;
    private long totalPayments;
    private double totalRevenue;
}
