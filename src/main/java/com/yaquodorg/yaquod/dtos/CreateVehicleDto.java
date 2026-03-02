package com.yaquodorg.yaquod.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateVehicleDto {

    // @ValidVIN
    @NotBlank(message = "vinNumber cannot be blank")
    private String vinNumber;

    @NotBlank(message = "plateNo cannot be blank")
    private String plateNo;

    @NotBlank(message = "color cannot be blank")
    private String color;

    @NotBlank(message = "car company cannot be blank")
    private String carCompany;

    @NotBlank(message = "car model cannot be blank")
    private String model;

    @Min(value = 1, message = "seats must be at least 1")
    @Max(value = 8, message = "seats must be at most 8")
    private int seats;
}
