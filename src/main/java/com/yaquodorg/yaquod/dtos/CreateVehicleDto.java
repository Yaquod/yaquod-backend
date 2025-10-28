package com.yaquodorg.yaquod.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateVehicleDto {
    private String vehicleUUID;

    private String plateNo;

    private String color;

    private String carCompany;

    private String model;

    private int seats;
}
