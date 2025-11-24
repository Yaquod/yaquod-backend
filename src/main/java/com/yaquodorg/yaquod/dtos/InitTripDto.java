package com.yaquodorg.yaquod.dtos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InitTripDto {
    private String vinNumber;

    private double requestId;

    private double startLong;

    private double startLat;

    private double endLong;

    private double endLat;
}
