package com.yaquodorg.yaquod.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleStreamLocationDto {
    // @ValidVIN
    private String vinNumber;

    private long tripId;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;
}
