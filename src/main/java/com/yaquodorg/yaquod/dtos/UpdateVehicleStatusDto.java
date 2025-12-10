package com.yaquodorg.yaquod.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.utils.ValidVIN;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVehicleStatusDto {
    @ValidVIN
    private String vinNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "Status cannot be blank")
    private VehicleStatus status;
}
