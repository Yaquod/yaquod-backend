package com.yaquodorg.yaquod.dtos.request;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private Long id;
    private RequestStatus status;
    private Timestamp createdAt;
    private double estimatedTime;
    private double estimatedFare;
    private Double pickupLong;
    private Double pickupLat;
    private Double destinationLong;
    private Double destinationLat;
    private Long userId;
    private Long tripId;
    private Long vehicleId;

    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .estimatedTime(r.getEstimatedTime())
                .estimatedFare(r.getEstimatedFare())
                .pickupLong(r.getStartLocation() != null ? r.getStartLocation().getX() : null)
                .pickupLat(r.getStartLocation() != null ? r.getStartLocation().getY() : null)
                .destinationLong(
                        r.getDestinationLocation() != null
                                ? r.getDestinationLocation().getX()
                                : null)
                .destinationLat(
                        r.getDestinationLocation() != null
                                ? r.getDestinationLocation().getY()
                                : null)
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .tripId(r.getTrip() != null ? r.getTrip().getId() : null)
                .vehicleId(
                        r.getTrip() != null && r.getTrip().getVehicle() != null
                                ? r.getTrip().getVehicle().getId()
                                : null)
                .build();
    }
}
