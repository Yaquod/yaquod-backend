package com.yaquodorg.yaquod.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import lombok.Data;

@Data
public class UpdateVehicleStatusDto {
    private String vehicleUUID;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private VehicleStatus status;


}
