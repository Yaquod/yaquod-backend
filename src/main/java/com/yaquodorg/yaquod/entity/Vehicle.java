package com.yaquodorg.yaquod.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
    @JsonIgnore
    private Geometry lastUpdatedLocation;

    @Column
    private Timestamp lastUpdatedLocationAt;

    @Column
    private double lastUpdatedLong;

    @Column
    private double lastUpdatedLat;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<Trip> trips = new ArrayList<>();


}
