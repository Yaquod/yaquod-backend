package com.yaquodorg.yaquod.dtos.trip;

import com.yaquodorg.yaquod.entity.TripStatus;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDto {
    private Long id;
    private TripStatus status;
    private Timestamp startedAt;
    private Timestamp endedAt;
    private Timestamp updatedAt;

    private Double startLong;
    private Double startLat;
    private Double endLong;
    private Double endLat;

    private BigDecimal amount;
    private String currency;

    private Integer ratingValue;

    private String carCompany;
    private String model;
    private String color;
}
