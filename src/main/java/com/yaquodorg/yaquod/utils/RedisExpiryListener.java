package com.yaquodorg.yaquod.utils;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisExpiryListener implements MessageListener {
    private final RequestService requestService;
    private final TripService tripService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Received expired key event for key: {}", expiredKey);

        if (expiredKey.startsWith("request:timeout:")) {
            Long requestId = Long.parseLong(expiredKey.replace("request:timeout:", ""));
            Request request = requestService.getRequest(requestId);
            Trip trip = tripService.getTripByRequestId(requestId);
            if (request != null
                    && request.getStatus() == com.yaquodorg.yaquod.entity.RequestStatus.PENDING) {
                requestService.updateRequestStatus(requestId, RequestStatus.TIMEOUT);
                tripService.updateTripStatus(trip.getId(), TripStatus.CANCELLED_BY_SYSTEM);
                log.info(
                        "Request {} has expired and was pending. Updated status to TIMEOUT and"
                                + " cancelled associated trip.",
                        requestId);

            } else {
                log.info(
                        "Request {} has expired but is not pending (status: {}). No action taken.",
                        requestId,
                        request != null ? request.getStatus() : "null");
            }
        }
    }
}
