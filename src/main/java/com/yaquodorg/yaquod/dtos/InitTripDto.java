package com.yaquodorg.yaquod.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InitTripDto {
  // @ValidVIN
  private String vinNumber;

  @NotNull(message = "Request ID cannot be null")
  private Long requestId;

  @NotNull
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  private double startLong;

  @NotNull
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  private double startLat;

  @NotNull
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  private double endLong;

  @NotNull
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  private double endLat;
}
