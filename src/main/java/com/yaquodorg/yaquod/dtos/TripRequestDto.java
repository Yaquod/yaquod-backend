package com.yaquodorg.yaquod.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripRequestDto {
    @NotNull(message = "startLong cannot be null")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double startLong;

    @NotNull(message = "startLat cannot be null")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double startLat;

    @NotNull(message = "endLong cannot be null")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double endLong;

    @NotNull(message = "endLat cannot be null")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double endLat;

}
