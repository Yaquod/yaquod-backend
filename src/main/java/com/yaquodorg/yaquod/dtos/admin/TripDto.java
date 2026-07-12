package com.yaquodorg.yaquod.dtos.admin;

import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDto {
    private Long id;
    private TripStatus status;
    private Timestamp startedAt;
    private Timestamp endedAt;
    private Timestamp updatedAt;
    private Long userId;
    private Long vehicleId;
    private Long paymentId;
    private Double pickupLong;
    private Double pickupLat;
    private Double destinationLong;
    private Double destinationLat;

    public static TripDto fromEntity(Trip t) {
        return TripDto.builder()
                .id(t.getId())
                .status(t.getStatus())
                .startedAt(t.getStartedAt())
                .endedAt(t.getEndedAt())
                .updatedAt(t.getUpdatedAt())
                .userId(t.getUser() != null ? t.getUser().getId() : null)
                .vehicleId(t.getVehicle() != null ? t.getVehicle().getId() : null)
                .paymentId(t.getPayment() != null ? t.getPayment().getId() : null)
                .pickupLong(
                        t.getRequest() != null && t.getRequest().getStartLocation() != null
                                ? t.getRequest().getStartLocation().getX()
                                : null)
                .pickupLat(
                        t.getRequest() != null && t.getRequest().getStartLocation() != null
                                ? t.getRequest().getStartLocation().getY()
                                : null)
                .destinationLong(
                        t.getRequest() != null && t.getRequest().getDestinationLocation() != null
                                ? t.getRequest().getDestinationLocation().getX()
                                : null)
                .destinationLat(
                        t.getRequest() != null && t.getRequest().getDestinationLocation() != null
                                ? t.getRequest().getDestinationLocation().getY()
                                : null)
                .build();
    }
}
