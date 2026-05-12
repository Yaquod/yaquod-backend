package com.yaquodorg.yaquod.utils;

import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisExpiryListener implements MessageListener {
    private final RequestService requestService;
    private final TripService tripService;
    private final VehicleService vehicleService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Received expired key event for key: {}", expiredKey);

        if (expiredKey.startsWith("request:timeout:")) {
            String requestIdValue = expiredKey.replace("request:timeout:", "");
            Long requestId;

            try {
                requestId = Long.parseLong(requestIdValue);
            } catch (NumberFormatException ex) {
                log.warn("Ignoring expired key with invalid request id format: {}", expiredKey, ex);
                return;
            }

            try {
                Request request = requestService.getRequest(requestId);

                if (request != null && request.getStatus() == RequestStatus.COMPLETED) {
                    Trip trip = tripService.getTripByRequestId(requestId);
                    Vehicle vehicle = trip.getVehicle();

                    handleTimeout(requestId, trip, vehicle);

                    log.info(
                            "Request {} has expired and was pending. Updated status to TIMEOUT and"
                                    + " cancelled associated trip.",
                            requestId);
                } else {
                    log.info(
                            "Request {} has expired but is not completed (status: {}). No action"
                                    + " taken.",
                            requestId,
                            request != null ? request.getStatus() : "null");
                }
            } catch (RuntimeException ex) {
                log.warn(
                        "Ignoring expired key for unknown or deleted request/trip. key: {},"
                                + " requestId: {}",
                        expiredKey,
                        requestId,
                        ex);
                return;
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTimeout(Long requestId, Trip trip, Vehicle vehicle) {
        requestService.updateRequestStatus(requestId, RequestStatus.TIMEOUT);
        tripService.updateTripStatus(trip.getId(), TripStatus.CANCELLED_BY_SYSTEM);
        vehicleService.updateVehicleStatus(vehicle.getVinNumber(), VehicleStatus.IDLE);
    }
}
