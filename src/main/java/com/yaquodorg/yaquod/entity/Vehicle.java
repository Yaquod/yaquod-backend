package com.yaquodorg.yaquod.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

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
    // @Column(unique = true, length = 17, columnDefinition = "CHAR(17)")     slightly more
    // efficient for storage and comparison
    private String vinNumber;

    @Column private String plateNo;

    // TODO: Should be enum later
    @Column private String color;

    @Column private String carCompany;

    @Column private String model;

    @Column private int seats;

    @Column
    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.IDLE;

    @Column private Timestamp lastUpdatedStatusAt;

    @Column(columnDefinition = "geometry")
    @JsonIgnore
    private Geometry lastUpdatedLocation;

    @Column private Timestamp lastUpdatedLocationAt;

    @Column private double lastUpdatedLong;

    @Column private double lastUpdatedLat;

    @Column(unique = true, nullable = false)
    private String apiKey;

    @JsonIgnore
    @Column(nullable = false)
    private String apiSecretHash;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column private Timestamp lastAuthenticatedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", nullable = false)
    private User createdByAdmin;

    @JsonIgnore
    @OneToMany(
            mappedBy = "vehicle",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Trip> trips = new ArrayList<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "vehicle",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();
}
