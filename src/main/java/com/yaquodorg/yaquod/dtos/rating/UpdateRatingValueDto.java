package com.yaquodorg.yaquod.dtos.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRatingValueDto {
    @NotNull(message = "ratingValue cannot be null")
    @Min(value = 0, message = "rating value must be at least 0")
    @Max(value = 5, message = "rating value must be at most 5")
    private Integer ratingValue;
}
