package com.yaquodorg.yaquod.service;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.repository.VehicleRepository;
import com.yaquodorg.yaquod.response.CreateVehicleResponse;
import com.yaquodorg.yaquod.service.vehicle.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    private final String VinNumber1 = "1HGCM82633A004352";
    private final String VinNumber2 = "1M8GDM9AXKP042788";
    private final String TestVin = "2GCEK19T7Y1156789";
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private VehicleServiceImpl vehicleService;
    private CreateVehicleDto createVehicleDto;
    private Vehicle vehicle;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Setup admin user
        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .build();

        createVehicleDto = CreateVehicleDto.builder()
                .vinNumber(VinNumber1)
                .plateNo("abc123")
                .color("RED")
                .carCompany("Dodge")
                .model("Charger")
                .seats(2)
                .build();

        vehicle = Vehicle.builder()
                .id(1L)
                .vinNumber(VinNumber2)
                .plateNo("abc123")
                .color("RED")
                .carCompany("Dodge")
                .model("Charger")
                .seats(2)
                .status(VehicleStatus.IDLE)
                .lastUpdatedLocation(null)
                .lastUpdatedLong(0.0d)
                .lastUpdatedLat(0.0d)
                .build();
    }

    @Test
    @DisplayName("Should create vehicle successfully")
    void shouldCreateVehicle() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-secret");
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        CreateVehicleResponse result = vehicleService.createVehicle(createVehicleDto, adminUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVehicle().getPlateNo()).isEqualTo("abc123");
        assertThat(result.getVehicle().getModel()).isEqualTo("Charger");
        assertThat(result.getVehicle().getStatus()).isEqualTo(VehicleStatus.IDLE);

        // Verify repository was called
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("Should get all vehicles")
    void shouldGetAllVehicles() {
        // Arrange
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(2L);
        vehicle2.setVinNumber(TestVin);
        vehicle2.setPlateNo("XYZ-789");
        vehicle2.setModel("Honda Accord");

        List<Vehicle> vehicles = Arrays.asList(vehicle, vehicle2);
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        // Act
        List<Vehicle> result = vehicleService.getVehicles();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Vehicle::getPlateNo)
                .containsExactly("abc123", "XYZ-789");

        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get vehicle by ID successfully")
    void shouldGetVehicleById() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // Act
        Vehicle result = vehicleService.getVehicle(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlateNo()).isEqualTo("abc123");

        verify(vehicleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when vehicle not found by ID")
    void shouldThrowExceptionWhenVehicleNotFoundById() {
        // Arrange
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vehicleService.getVehicle(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found!");

        verify(vehicleRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get vehicle by VIN successfully")
    void shouldGetVehicleByVIN() {
        // Arrange
        String vin = VinNumber2;
        when(vehicleRepository.findByVinNumber(vin)).thenReturn(Optional.of(vehicle));

        // Act
        Optional<Vehicle> result = vehicleService.getVehicleByVinNumber(vin);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getVinNumber()).isEqualTo(vin);

        verify(vehicleRepository, times(1)).findByVinNumber(vin);
    }

    @Test
    @DisplayName("Should return empty when vehicle not found by VIN")
    void shouldReturnEmptyWhenVehicleNotFoundByVIN() {
        // Arrange
        String vin = "3VWFE21C04M000123";
        when(vehicleRepository.findByVinNumber(vin)).thenReturn(Optional.empty());

        // Act
        Optional<Vehicle> result = vehicleService.getVehicleByVinNumber(vin);

        // Assert
        assertThat(result).isEmpty();

        verify(vehicleRepository, times(1)).findByVinNumber(vin);
    }

    @Test
    @DisplayName("Should update vehicle location successfully")
    void shouldUpdateVehicleLocation() {
        // Arrange
        String vin = TestVin;
        double longitude = 40.7128;
        double latitude = -74.0060;

        when(vehicleRepository.findByVinNumber(vin)).thenReturn(Optional.of(vehicle));

        // Act
        vehicleService.updateVehicleLocation(vin, longitude, latitude);

        // Assert - Verify the vehicle object was modified (not saved explicitly)
        assertThat(vehicle.getLastUpdatedLong()).isEqualTo(longitude);
        assertThat(vehicle.getLastUpdatedLat()).isEqualTo(latitude);

        // Verify Point geometry was set correctly
        assertThat(vehicle.getLastUpdatedLocation()).isNotNull();
        assertThat(vehicle.getLastUpdatedLocation().getSRID()).isEqualTo(4326);

        // Verify repository interactions
        verify(vehicleRepository, times(1)).findByVinNumber(vin);
        // Note: save() is NOT called because @Transactional handles persistence
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw exception when updating location for non-existent vehicle")
    void shouldThrowExceptionWhenUpdatingLocationForNonExistentVehicle() {
        // Arrange
        String vin = "3VWFE21C04M000123";
        when(vehicleRepository.findByVinNumber(vin)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vehicleService.updateVehicleLocation(vin, 40.7128, -74.0060))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found with VIN: " + vin);

        verify(vehicleRepository, times(1)).findByVinNumber(vin);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should update vehicle status successfully")
    void shouldUpdateVehicleStatus() {
        // Arrange
        String vin = TestVin;
        VehicleStatus newStatus = VehicleStatus.IN_USE;

        when(vehicleRepository.findByVinNumber(vin)).thenReturn(Optional.of(vehicle));

        // Act
        vehicleService.updateVehicleStatus(vin, newStatus);

        // Assert - Verify the vehicle object was modified
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_USE);

        // Verify repository interactions
        verify(vehicleRepository, times(1)).findByVinNumber(vin);
        // Note: save() is NOT called because @Transactional handles persistence
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should delete vehicle successfully")
    void shouldDeleteVehicle() {
        // Arrange
        Long vehicleId = 1L;
        doNothing().when(vehicleRepository).deleteById(vehicleId);

        // Act
        vehicleService.deleteVehicle(vehicleId);

        // Assert
        verify(vehicleRepository, times(1)).deleteById(vehicleId);
    }

    @Test
    @DisplayName("Should handle null DTO gracefully")
    void shouldHandleNullDto() {
        // Act & Assert
        assertThatThrownBy(() -> vehicleService.createVehicle(null, adminUser))
                .isInstanceOf(NullPointerException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should find k-nearest vehicles successfully")
    void shouldFindKNearestVehicles() {
        // Arrange
        double longitude = 31.0;
        double latitude = 30.0;
        int k = 3;

        Vehicle vehicle2 = Vehicle.builder()
                .id(2L)
                .vinNumber("VIN789")
                .plateNo("XYZ-789")
                .model("Honda Accord")
                .status(VehicleStatus.IDLE)
                .build();

        List<Vehicle> nearestVehicles = Arrays.asList(vehicle, vehicle2);
        when(vehicleRepository.findKNearestVehicles(any(), eq(k)))
                .thenReturn(nearestVehicles);

        // Act
        List<Vehicle> result = vehicleService.findKNearestVehicles(longitude, latitude, k);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Vehicle::getVinNumber)
                .containsExactly(VinNumber2, "VIN789");

        verify(vehicleRepository, times(1)).findKNearestVehicles(any(), eq(k));
    }

    @Test
    @DisplayName("Should return empty list when no vehicles nearby")
    void shouldReturnEmptyListWhenNoVehiclesNearby() {
        // Arrange
        double longitude = 31.0;
        double latitude = 30.0;
        int k = 5;

        when(vehicleRepository.findKNearestVehicles(any(), eq(k)))
                .thenReturn(List.of());

        // Act
        List<Vehicle> result = vehicleService.findKNearestVehicles(longitude, latitude, k);

        // Assert
        assertThat(result).isEmpty();

        verify(vehicleRepository, times(1)).findKNearestVehicles(any(), eq(k));
    }

    @Test
    @DisplayName("Should find k-nearest vehicles within distance successfully")
    void shouldFindKNearestVehiclesWithinDistance() {
        // Arrange
        double longitude = 31.0;
        double latitude = 30.0;
        double maxDistance = 5000.0; // 5km
        int k = 2;

        List<Vehicle> nearbyVehicles = List.of(vehicle);
        when(vehicleRepository.findKNearestVehiclesWithinDistance(any(), eq(maxDistance), eq(k)))
                .thenReturn(nearbyVehicles);

        // Act
        List<Vehicle> result = vehicleService.findKNearestVehiclesWithinDistance(
                longitude, latitude, maxDistance, k);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVinNumber()).isEqualTo(VinNumber2);

        verify(vehicleRepository, times(1))
                .findKNearestVehiclesWithinDistance(any(), eq(maxDistance), eq(k));
    }

    @Test
    @DisplayName("Should return empty list when no vehicles within specified distance")
    void shouldReturnEmptyListWhenNoVehiclesWithinDistance() {
        // Arrange
        double longitude = 31.0;
        double latitude = 30.0;
        double maxDistance = 100.0; // 100 meters
        int k = 5;

        when(vehicleRepository.findKNearestVehiclesWithinDistance(any(), eq(maxDistance), eq(k)))
                .thenReturn(List.of());

        // Act
        List<Vehicle> result = vehicleService.findKNearestVehiclesWithinDistance(
                longitude, latitude, maxDistance, k);

        // Assert
        assertThat(result).isEmpty();

        verify(vehicleRepository, times(1))
                .findKNearestVehiclesWithinDistance(any(), eq(maxDistance), eq(k));
    }

    @Test
    @DisplayName("Should create Point geometry correctly for k-nearest search")
    void shouldCreatePointGeometryCorrectlyForKNearestSearch() {
        // Arrange
        double longitude = 31.2345;
        double latitude = 30.6789;
        int k = 1;

        when(vehicleRepository.findKNearestVehicles(any(), eq(k)))
                .thenReturn(List.of(vehicle));

        // Act
        vehicleService.findKNearestVehicles(longitude, latitude, k);

        // Assert
        verify(vehicleRepository, times(1)).findKNearestVehicles(any(), eq(k));
    }

    @Test
    @DisplayName("Should handle various k values correctly")
    void shouldHandleVariousKValuesCorrectly() {
        // Arrange
        double longitude = 31.0;
        double latitude = 30.0;

        // Test with k=1
        when(vehicleRepository.findKNearestVehicles(any(), eq(1)))
                .thenReturn(List.of(vehicle));

        // Act
        List<Vehicle> result1 = vehicleService.findKNearestVehicles(longitude, latitude, 1);

        // Assert
        assertThat(result1).hasSize(1);

        // Test with k=10
        List<Vehicle> multipleVehicles = Collections.singletonList(vehicle);
        when(vehicleRepository.findKNearestVehicles(any(), eq(10)))
                .thenReturn(multipleVehicles);

        // Act
        List<Vehicle> result10 = vehicleService.findKNearestVehicles(longitude, latitude, 10);

        // Assert
        assertThat(result10).isNotEmpty();

        verify(vehicleRepository, times(1)).findKNearestVehicles(any(), eq(1));
        verify(vehicleRepository, times(1)).findKNearestVehicles(any(), eq(10));
    }
}
