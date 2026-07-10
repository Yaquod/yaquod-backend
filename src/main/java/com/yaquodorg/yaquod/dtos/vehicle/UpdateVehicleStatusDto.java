package com.yaquodorg.yaquod.dtos.vehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVehicleStatusDto {
    private String vinNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "Status cannot be blank")
    private VehicleStatus status;
}
