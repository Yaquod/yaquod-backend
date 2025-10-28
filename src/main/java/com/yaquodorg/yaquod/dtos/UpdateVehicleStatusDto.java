package com.yaquodorg.yaquod.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateVehicleStatusDto {
    private String vehicleUUID;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotBlank(message = "Status cannot be blank")
    private VehicleStatus status;
}
