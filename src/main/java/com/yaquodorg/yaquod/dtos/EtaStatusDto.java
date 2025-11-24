package com.yaquodorg.yaquod.dtos;

import com.yaquodorg.yaquod.entity.RequestStatus;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EtaStatusDto {
    private RequestStatus status;

    private String vinNumber;

    private long requestId;

    private double estimatedTime;

    private double estimatedFare;
}
