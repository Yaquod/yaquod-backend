package com.yaquodorg.yaquod.dtos;

import lombok.Data;

@Data
public class CreateVehicleDto {
    private String vehicleUUID;

    private String plateNo;

    private String color;

    private String carCompany;

    private String model;

    private int seats;
}
