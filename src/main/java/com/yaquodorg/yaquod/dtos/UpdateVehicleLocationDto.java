package com.yaquodorg.yaquod.dtos;

import lombok.Data;

@Data
public class UpdateVehicleLocationDto {
    private String vehicleUUID;
    private double longitude;
    private double latitude;
}
