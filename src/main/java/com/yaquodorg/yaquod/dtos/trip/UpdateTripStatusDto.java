package com.yaquodorg.yaquod.dtos.trip;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateTripStatusDto {
    private String vinNumber;

    private long tripId;

    private String tripStatus;
}
