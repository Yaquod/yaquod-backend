package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Trip;

import java.util.List;

public interface TripService {
    void createTrip(Request request, double startLong, double startLat, double endLong, double endLat);

    Trip getTripByRequestId(Long requestId);

    Trip getTripById(Long id);

    void deleteTripById(Long id);

//    Trip updateTrip(Long id, Trip updatedTrip);

    List<Trip> getAllTrips();

    List<Trip> getTripsByUserId(Long userId);

    List<Trip>getLastNTrips(int n);

    List <Trip>getTripsByVinNumber(String vinNumber);
}
