package com.yaquodorg.yaquod.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class VehicleDto {
  // @ValidVIN
  private String vinNumber;
}
