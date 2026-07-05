package com.yaquodorg.yaquod.dtos.trip;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripCancelDto {
    private String vinNumber;

    @NotNull(message = "Request ID cannot be null")
    private Long requestId;
}
