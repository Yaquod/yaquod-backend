package com.yaquodorg.yaquod.dtos.admin;

import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {
    private Long id;
    private String vinNumber;
    private String plateNo;
    private String color;
    private String carCompany;
    private String model;
    private int seats;
    private VehicleStatus status;
    private Timestamp lastUpdatedStatusAt;
    private Timestamp lastUpdatedLocationAt;
    private double lastUpdatedLong;
    private double lastUpdatedLat;
    private String apiKey;
    private Timestamp createdAt;
    private Timestamp lastAuthenticatedAt;
    private Long createdByAdminId;

    public static VehicleDto fromEntity(Vehicle v) {
        return VehicleDto.builder()
                .id(v.getId())
                .vinNumber(v.getVinNumber())
                .plateNo(v.getPlateNo())
                .color(v.getColor())
                .carCompany(v.getCarCompany())
                .model(v.getModel())
                .seats(v.getSeats())
                .status(v.getStatus())
                .lastUpdatedStatusAt(v.getLastUpdatedStatusAt())
                .lastUpdatedLocationAt(v.getLastUpdatedLocationAt())
                .lastUpdatedLong(v.getLastUpdatedLong())
                .lastUpdatedLat(v.getLastUpdatedLat())
                .apiKey(v.getApiKey())
                .createdAt(v.getCreatedAt())
                .lastAuthenticatedAt(v.getLastAuthenticatedAt())
                .createdByAdminId(
                        v.getCreatedByAdmin() != null ? v.getCreatedByAdmin().getId() : null)
                .build();
    }
}
