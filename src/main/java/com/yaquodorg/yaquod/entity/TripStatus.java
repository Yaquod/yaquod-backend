package com.yaquodorg.yaquod.entity;

public enum TripStatus {
    // Pre-Trip States
    INITIATED, VEHICLE_ON_WAY, VEHICLE_CLOSE, ARRIVED_AT_PICKUP,

    // Active Trip States
    PASSENGER_ONBOARD, IN_PROGRESS,

    // Completion States
    ARRIVED_AT_DESTINATION, COMPLETED,

    // Issue/Exception States
    PASSENGER_NO_SHOW, CANCELLED_BY_PASSENGER, CANCELLED_BY_SYSTEM,

    // Emergency/Problem States
    EMERGENCY, INCIDENT, VEHICLE_ISSUE, REFUNDED
}
