package com.yaquodorg.yaquod.service.request;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import java.util.List;

public interface RequestService {
    Request createRequest(
            Long userId, double startLong, double startLat, double endLong, double endLat);

    List<Request> getRequests();

    List<Request> getRequestsWithTripAndVehicle();

    List<Request> getUserRequests(Long userId);

    Request getRequest(Long requestId);

    void updateRequest(
            Long requestId,
            RequestStatus requestStatus,
            double estimatedTime,
            double estimatedFare);

    void updateRequestStatus(Long requestId, RequestStatus requestStatus);

    void deleteRequest(Long requestId);

    void cancelRequest(Long id, Long userId);

    void declineRequestById(Long id, Long userId);

    Request acceptRequestById(Long id, Long userId);

    long countRequestsByStatusIn(List<RequestStatus> statuses);
}
