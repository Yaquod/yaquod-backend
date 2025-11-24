package com.yaquodorg.yaquod.dtos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EtaStatusDto {
    // TODO: Should be converted to enum later
    private String status;

    private String vinNumber;

    private long requestId;

    private double estimatedTime;

    private double estimatedFare;
}
