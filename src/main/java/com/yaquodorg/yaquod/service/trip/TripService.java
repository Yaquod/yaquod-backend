package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.entity.Request;

public interface TripService {
    void createTrip(Request request, double startLong, double startLat, double endLong, double endLat);
}
