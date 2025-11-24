package com.yaquodorg.yaquod.dtos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InitTripDto {
    private String vinNumber;

    private long requestId;

    private long startLong;

    private long startLat;

    private long endLong;

    private long endLat;
}
