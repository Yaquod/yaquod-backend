package com.yaquodorg.yaquod.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.sql.Timestamp;

/**
 * The type Request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point startLocation;

    @JsonIgnore
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point destinationLocation;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column
    private double estimatedTime;

    @Column
    private double estimatedFare;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @JsonIgnore
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL)
    private Trip trip;
}
