package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.dtos.EtaStatusDto;
import com.yaquodorg.yaquod.entity.Request;

public interface TripService {

    void initTrip(Long requestId, double startLong, double startLat, double endLong, double endLat);

    EtaStatusDto checkStatus(long requestId);

    public Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat);


}
