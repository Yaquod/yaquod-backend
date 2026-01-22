package com.yaquodorg.yaquod.service.request;

import java.util.List;

import com.yaquodorg.yaquod.dtos.MoveVehicleDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;

public interface RequestService {
    Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat);

    List<Request> getRequests();

    List<Request> getUserRequests(Long userId);

    Request getRequest(Long requestId);

    void updateRequest(Long requestId, RequestStatus requestStatus, double estimatedTime, double estimatedFare);

    void updateRequestStatus(Long requestId, RequestStatus requestStatus);

    void deleteRequest(Long requestId);

    void declineRequestById(Long id, String token);

    Request acceptRequestById(Long id, String token);

    MoveVehicleDto generateVehicleMovementDto(Long requestId);
}
