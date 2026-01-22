package com.yaquodorg.yaquod.service.request;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import lombok.extern.java.Log;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private final RequestRepository requestRepository;

    private final UserService userService;
    private final TripService tripService;

    @Transactional
    @Override
    public Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat) {
        User user = userService.getUserById(userId);

        Point startPoint = geometryFactory.createPoint(new Coordinate(startLong, startLat));
        Point endPoint = geometryFactory.createPoint(new Coordinate(endLong, endLat));

        Request request = Request.builder().user(user).startLocation(startPoint).destinationLocation(endPoint).status(RequestStatus.PENDING).createdAt(new Timestamp(new Date().getTime())).build();

        Request savedRequest = requestRepository.save(request);
        tripService.createTrip(savedRequest, startLong, startLat, endLong, endLat);

        return savedRequest;
    }

    @Override
    public List<Request> getRequests() {
        return requestRepository.findAll();
    }

    @Override
    public List<Request> getUserRequests(Long userId) {
        User user = userService.getUserById(userId);
        return user.getRequests();
    }

    @Override
    public Request getRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found!"));
    }

    @Transactional
    @Override
    public void updateRequest(Long requestId, RequestStatus requestStatus, double estimatedTime, double estimatedFare) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found!"));

        request.setStatus(requestStatus);
        request.setEstimatedTime(estimatedTime);
        request.setEstimatedFare(estimatedFare);
    }

    @Override
    public void deleteRequest(Long requestId) {
        requestRepository.deleteById(requestId);
    }

    @Override
    public void declineRequestById(Long id, String token) {
        User user = userService.getUserByJwt(token);
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to decline this request");
        } else {
            request.setStatus(RequestStatus.DECLINED);
            log.info("Request with id {} has been declined.", id);
            long tripId = request.getTrip().getId().intValue();
            tripService.declineTripById(tripId);
            log.info("Declining associated trip with id {}.", tripId);
        }
    }

    @Override
    public Request acceptRequestById(Long id, String token) {
        User user = userService.getUserByJwt(token);
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to accept this request");
        } else {
            request.setStatus(RequestStatus.ACCEPTED);
            log.info("Request with id {} has been accepted.", id);
            long tripId = request.getTrip().getId().intValue();
            tripService.acceptTripById(tripId);
            log.info("Accepting associated trip with id {}.", tripId);
            return request;
        }

    }
}
