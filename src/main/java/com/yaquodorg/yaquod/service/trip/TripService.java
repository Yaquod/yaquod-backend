package com.yaquodorg.yaquod.service.trip;

import java.util.List;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;

public interface TripService {
    void createTrip(Request request, double startLong, double startLat, double endLong, double endLat);

    Trip getTripByRequestId(Long requestId);

    Trip getTripById(Long id);

    List<Trip> getAllTrips();

    List<Trip> getTripsByUserId(Long userId);

    List<Trip> getUserLastNTrips(int n, Long userId);

    List<Trip> getTripsByVinNumber(String vinNumber);

    void updateTripStatus(Long id, TripStatus tripStatus);

    void deleteTripById(Long id);

    void startTrip(Long requestId);
}
