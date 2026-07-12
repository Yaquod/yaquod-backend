package com.yaquodorg.yaquod.service.request;

import com.yaquodorg.yaquod.dtos.admin.RequestDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestService {
    Request createRequest(
            Long userId, double startLong, double startLat, double endLong, double endLat);

    List<Request> getRequests();

    List<Request> getRequestsWithTripAndVehicle();

    Page<RequestDto> getUserRequestsPaginated(Pageable pageable, Long userId);

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
