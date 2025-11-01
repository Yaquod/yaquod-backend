package com.yaquodorg.yaquod.entity;

import java.sql.Timestamp;

import org.locationtech.jts.geom.Geometry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String vinNumber;

    @Column
    private String plateNo;

    // TODO: Should be enum later
    @Column
    private String color;

    @Column
    private String carCompany;

    @Column
    private String model;

    @Column
    private int seats;

    @Column
    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.IDLE;

    @Column
    private Timestamp lastUpdatedStatusAt;

    @Column(columnDefinition = "geometry")
    private Geometry lastUpdatedLocation;

    @Column
    private Timestamp lastUpdatedLocationAt;

    @Column
    private double lastUpdatedLong;

    @Column
    private double lastUpdatedLat;
}
