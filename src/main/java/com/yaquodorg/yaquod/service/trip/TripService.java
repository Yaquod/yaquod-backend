package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.dtos.EtaStatusDto;

public interface TripService {

    void initTrip(long requestId, long startLong, long startLat, long endLong, long endLat);

    EtaStatusDto checkStatus(long requestId);
}
