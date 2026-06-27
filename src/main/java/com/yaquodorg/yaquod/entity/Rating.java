package com.yaquodorg.yaquod.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ratings")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Min(value = 0, message = "Rating value must be at least 0")
    @Max(value = 5, message = "Rating value must be at most 5")
    private int ratingValue;

    @Column private String comment;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "trip_id", referencedColumnName = "id")
    private Trip trip;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    private Vehicle vehicle;
}
