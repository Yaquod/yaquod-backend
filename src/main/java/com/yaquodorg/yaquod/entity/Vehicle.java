package com.yaquodorg.yaquod.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String vehicleUUID;

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

    @Column(columnDefinition = "geometry")
    private Geometry lastUpdatedLocation;

    @Column
    private double lastUpdatedLong;

    @Column
    private double lastUpdatedLat;
}
