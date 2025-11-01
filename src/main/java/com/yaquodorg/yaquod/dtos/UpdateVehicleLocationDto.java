package com.yaquodorg.yaquod.dtos;

import lombok.Data;

@Data
public class UpdateVehicleLocationDto {
    private String vinNumber;
    private double longitude;
    private double latitude;
}
