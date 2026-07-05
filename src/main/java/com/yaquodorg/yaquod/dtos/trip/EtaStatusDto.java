package com.yaquodorg.yaquod.dtos.trip;

import com.yaquodorg.yaquod.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EtaStatusDto {
    private RequestStatus status;

    private String vinNumber;

    @NotNull(message = "Request ID cannot be null")
    private Long requestId;

    @NotNull(message = "Estimated time cannot be null")
    private double estimatedTime;

    @NotNull(message = "Estimated fare cannot be null")
    private double estimatedFare;
}
