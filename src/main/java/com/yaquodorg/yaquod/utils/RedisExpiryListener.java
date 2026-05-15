package com.yaquodorg.yaquod.utils;

import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.request.timeout-prefix}")
    private String REQUEST_TIMEOUT_PREFIX;

    @Value("${app.eta.timeout-prefix}")
    private String ETA_TIMEOUT_PREFIX;

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Received expired key event for key: {}", expiredKey);

        if (expiredKey.startsWith(REQUEST_TIMEOUT_PREFIX)) {
            String requestIdValue = expiredKey.replace(REQUEST_TIMEOUT_PREFIX, "");
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

                    handleRequestTimeout(requestId, trip, vehicle);

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
            }
        } else if (expiredKey.startsWith(ETA_TIMEOUT_PREFIX)) {
            String requestIdValue = expiredKey.replace(ETA_TIMEOUT_PREFIX, "");
            Long requestId;
            try {
                requestId = Long.parseLong(requestIdValue);
            } catch (NumberFormatException ex) {
                log.warn("Ignoring expired key with invalid request id format: {}", expiredKey, ex);
                return;
            }

            try {
                Request request = requestService.getRequest(requestId);
                if (request != null && request.getStatus() == RequestStatus.PENDING) {
                    Trip trip = tripService.getTripByRequestId(requestId);
                    Vehicle vehicle = trip.getVehicle();

                    handleEtaTimeout(requestId, trip, vehicle);
                    log.warn(
                            "ETA for request {} has expired. Updated request status to TIMEOUT and"
                                    + " cancelled associated trip.",
                            requestId);
                } else {
                    log.info(
                            "ETA for request {} has expired but request is not completed (status:"
                                    + " {}). No action taken.",
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
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRequestTimeout(Long requestId, Trip trip, Vehicle vehicle) {
        requestService.updateRequestStatus(requestId, RequestStatus.TIMEOUT);
        tripService.updateTripStatus(trip.getId(), TripStatus.CANCELLED_BY_SYSTEM);
        vehicleService.updateVehicleStatus(vehicle.getVinNumber(), VehicleStatus.IDLE);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEtaTimeout(Long requestId, Trip trip, Vehicle vehicle) {
        requestService.updateRequestStatus(requestId, RequestStatus.TIMEOUT);
        tripService.updateTripStatus(trip.getId(), TripStatus.CANCELLED_BY_SYSTEM);
        vehicleService.updateVehicleStatus(vehicle.getVinNumber(), VehicleStatus.IDLE);
    }
}
