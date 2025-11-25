package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.dtos.EtaStatusDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;

public interface TripService {

    void initTrip(Long requestId, double startLong, double startLat, double endLong, double endLat);

    EtaStatusDto checkStatus(Long requestId);

    Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat);

    RequestStatus getRequestStatusByRequestId(Long requestId);

}
