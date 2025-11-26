package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;

public interface TripService {

    void createTrip(Long requestId, double startLong, double startLat, double endLong, double endLat);

    Request getRequest(Long requestId);

    Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat);

    void updateRequest(Long requestId, RequestStatus requestStatus, double estimatedTime, double estimatedFare);
}
