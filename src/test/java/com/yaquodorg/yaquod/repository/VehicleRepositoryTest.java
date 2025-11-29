package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 * <p>
 * Integration tests for VehicleRepository
 * Uses real database (H2 in-memory or Testcontainers)
 * Tests JPA queries and database interactions
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("VehicleRepository Integration Tests")
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Vehicle vehicle1;
    private Vehicle vehicle2;

    @BeforeEach
    void setUp() {
        // Clean database
        vehicleRepository.deleteAll();

        // Setup test data
        vehicle1 = Vehicle.builder()
                .vinNumber("vin-001")
                .plateNo("ABC-123")
                .model("Toyota Camry")
                .seats(4)
                .status(VehicleStatus.IDLE)
                .lastUpdatedLong(40.7128)
                .lastUpdatedLat(-74.0060)
                .build();

        vehicle2 = Vehicle.builder()
                .vinNumber("vin-002")
                .plateNo("XYZ-789")
                .model("Honda Accord")
                .seats(5)
                .status(VehicleStatus.IN_USE)
                .lastUpdatedLong(34.0522)
                .lastUpdatedLat(-118.2437)
                .build();
    }

    @Test
    @DisplayName("Should save vehicle successfully")
    void shouldSaveVehicle() {
        // Act
        Vehicle saved = vehicleRepository.save(vehicle1);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVinNumber()).isEqualTo("vin-001");
        assertThat(saved.getPlateNo()).isEqualTo("ABC-123");
    }

    @Test
    @DisplayName("Should find vehicle by VIN")
    void shouldFindVehicleByVIN() {
        // Arrange
        entityManager.persist(vehicle1);
        entityManager.flush();

        // Act
        Optional<Vehicle> found = vehicleRepository.findByVinNumber("vin-001");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getPlateNo()).isEqualTo("ABC-123");
    }

    @Test
    @DisplayName("Should return empty when vehicle VIN not found")
    void shouldReturnEmptyWhenVINNotFound() {
        // Act
        Optional<Vehicle> found = vehicleRepository.findByVinNumber("non-existent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all vehicles")
    void shouldFindAllVehicles() {
        // Arrange
        entityManager.persist(vehicle1);
        entityManager.persist(vehicle2);
        entityManager.flush();

        // Act
        List<Vehicle> vehicles = vehicleRepository.findAll();

        // Assert
        assertThat(vehicles).hasSize(2);
        assertThat(vehicles).extracting(Vehicle::getPlateNo)
                .containsExactlyInAnyOrder("ABC-123", "XYZ-789");
    }

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("Should find vehicles by status")
    // void shouldFindVehiclesByStatus() {
    // // Arrange
    // entityManager.persist(vehicle1);
    // entityManager.persist(vehicle2);
    // entityManager.flush();
    //
    // // Act
    // List<Vehicle> availableVehicles = vehicleRepository
    // .findByStatus(VehicleStatus.IDLE);
    //
    // // Assert
    // assertThat(availableVehicles).hasSize(1);
    // assertThat(availableVehicles.get(0).getPlateNo()).isEqualTo("ABC-123");
    // }

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("Should find vehicle by license plate")
    // void shouldFindVehicleByLicensePlate() {
    // // Arrange
    // entityManager.persist(vehicle1);
    // entityManager.flush();
    //
    // // Act
    // Optional<Vehicle> found = vehicleRepository.findByLicensePlate("ABC-123");
    //
    // // Assert
    // assertThat(found).isPresent();
    // assertThat(found.get().getModel()).isEqualTo("Toyota Camry");
    // }

    @Test
    @DisplayName("Should update vehicle successfully")
    void shouldUpdateVehicle() {
        // Arrange
        Vehicle saved = entityManager.persist(vehicle1);
        entityManager.flush();

        // Act
        saved.setStatus(VehicleStatus.ON_WAY);
        saved.setLastUpdatedLong(51.5074);
        saved.setLastUpdatedLat(-0.1278);
        Vehicle updated = vehicleRepository.save(saved);

        // Assert
        assertThat(updated.getStatus()).isEqualTo(VehicleStatus.ON_WAY);
        assertThat(updated.getLastUpdatedLong()).isEqualTo(51.5074);
        assertThat(updated.getLastUpdatedLat()).isEqualTo(-0.1278);
    }

    @Test
    @DisplayName("Should delete vehicle successfully")
    void shouldDeleteVehicle() {
        // Arrange
        Vehicle saved = entityManager.persist(vehicle1);
        entityManager.flush();
        Long vehicleId = saved.getId();

        // Act
        vehicleRepository.deleteById(vehicleId);

        // Assert
        Optional<Vehicle> found = vehicleRepository.findById(vehicleId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if vehicle exists by ID")
    void shouldCheckIfVehicleExists() {
        // Arrange
        Vehicle saved = entityManager.persist(vehicle1);
        entityManager.flush();

        // Act
        boolean exists = vehicleRepository.existsById(saved.getId());
        boolean notExists = vehicleRepository.existsById(999L);

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should count vehicles")
    void shouldCountVehicles() {
        // Arrange
        entityManager.persist(vehicle1);
        entityManager.persist(vehicle2);
        entityManager.flush();

        // Act
        long count = vehicleRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle concurrent updates correctly")
    void shouldHandleConcurrentUpdates() {
        // Arrange
        Vehicle saved = entityManager.persist(vehicle1);
        entityManager.flush();
        entityManager.clear();

        // Act - Simulate two concurrent updates
        Vehicle vehicle1Copy = vehicleRepository.findById(saved.getId()).get();
        Vehicle vehicle2Copy = vehicleRepository.findById(saved.getId()).get();

        vehicle1Copy.setStatus(VehicleStatus.IN_USE);
        vehicleRepository.save(vehicle1Copy);

        vehicle2Copy.setStatus(VehicleStatus.OUT_OF_SERVICE);
        vehicleRepository.save(vehicle2Copy);

        // Assert - Last write wins
        Vehicle result = vehicleRepository.findById(saved.getId()).get();
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.OUT_OF_SERVICE);
    }
}
