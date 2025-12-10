package com.yaquodorg.yaquod.dtos;

import com.yaquodorg.yaquod.utils.ValidVIN;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVehicleLocationDto {
    @ValidVIN
    private String vinNumber;
    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double longitude;
    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double latitude;
}
