package com.yaquodorg.yaquod.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private Integer ratingValue;
    private String comment;
    private Long userId;
    private Long tripId;
    private Long vehicleId;
}
