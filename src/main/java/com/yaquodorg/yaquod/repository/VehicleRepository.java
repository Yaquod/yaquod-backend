package com.yaquodorg.yaquod.repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yaquodorg.yaquod.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVinNumber(String vinNumber);

    // Find k-nearest vehicles to a given location
    @Query(value = """
            SELECT v.*
            FROM vehicles v
            WHERE v.last_updated_location IS NOT NULL
            AND v.status = 'IDLE'
            ORDER BY ST_Distance(v.last_updated_location, :point)
            LIMIT :limit
            """, nativeQuery = true)
    List<Vehicle> findKNearestVehicles(@Param("point") Point point, @Param("limit") int limit);

    // Find k-nearest vehicles within a maximum distance in meters
    @Query(value = """
            SELECT v.*
            FROM vehicles v
            WHERE v.last_updated_location IS NOT NULL
            AND v.status = 'IDLE'
            AND ST_DWithin(v.last_updated_location::geography, :point::geography, :maxDistance)
            ORDER BY ST_Distance(v.last_updated_location, :point)
            LIMIT :limit
            """, nativeQuery = true)
    List<Vehicle> findKNearestVehiclesWithinDistance(
            @Param("point") Point point,
            @Param("maxDistance") double maxDistanceMeters,
            @Param("limit") int limit);
}
