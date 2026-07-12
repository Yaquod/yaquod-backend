package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.dtos.trip.TripDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface TripService {
    void createTrip(
            Request request, double startLong, double startLat, double endLong, double endLat);

    Trip getTripByRequestId(Long requestId);

    Trip getTripById(Long id);

    List<Trip> getAllTrips();

    List<Trip> getAllTripsWithAssociations();

    List<Trip> getTripsByUserId(Long userId);

    Page<TripDto> getUserTripsPaginated(Pageable pageable, Long userId);

    List<Trip> getTripsByVinNumber(String vinNumber);

    void updateTripStatus(Long id, TripStatus tripStatus);

    void deleteTripById(Long id);

    SseEmitter subscribeToLocationStream(Long tripId);

    void unsubscribeToLocationStream(Long tripId);

    void broadcastLocationStream(Long tripId, double latitude, double longitude);

    void startTrip(Long requestId);

    void endTrip(Long requestId);

    long countTrips();

    long countTripsByStatusIn(List<TripStatus> statuses);
}
