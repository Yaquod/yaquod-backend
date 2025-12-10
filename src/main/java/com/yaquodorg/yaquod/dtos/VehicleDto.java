package com.yaquodorg.yaquod.dtos;

import com.yaquodorg.yaquod.utils.ValidVIN;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleDto {
    @ValidVIN
    private String vinNumber;
}
