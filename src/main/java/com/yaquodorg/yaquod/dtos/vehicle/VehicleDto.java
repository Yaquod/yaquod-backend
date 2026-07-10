package com.yaquodorg.yaquod.dtos.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class VehicleDto {
    private String vinNumber;
}
