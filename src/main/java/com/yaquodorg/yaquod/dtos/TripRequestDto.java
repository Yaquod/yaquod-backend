package com.yaquodorg.yaquod.dtos;

import lombok.Data;

@Data
public class TripRequestDto {
    private double startLong;

    private double startLat;

    private double endLong;

    private double endLat;

}
