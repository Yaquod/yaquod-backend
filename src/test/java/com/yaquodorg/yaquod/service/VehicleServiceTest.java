package com.yaquodorg.yaquod.service;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.repository.VehicleRepository;
import com.yaquodorg.yaquod.service.vehicle.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private CreateVehicleDto createVehicleDto;
    private Vehicle vehicle;

    private  final  String VinNumber1 = "1HGCM82633A004352";
    private  final  String TestVin = "2GCEK19T7Y1156789";


    @BeforeEach
    void setUp() {
        createVehicleDto = CreateVehicleDto.builder()
                .vinNumber(VinNumber1)
                .plateNo("abc123")
                .color("RED")
                .carCompany("Dodge")
                .model("Charger")
                .seats(2)
                .build();

        String vinNumber2 = "1M8GDM9AXKP042788";
        vehicle = Vehicle.builder()
                .id(1L)
                .vinNumber(vinNumber2)
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
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        Vehicle result = vehicleService.createVehicle(createVehicleDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPlateNo()).isEqualTo("abc123");
        assertThat(result.getModel()).isEqualTo("Charger");
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.IDLE);

        // Verify repository was called
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
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
        String vin = VinNumber1;
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

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("Should handle null DTO gracefully")
    // void shouldHandleNullDto() {
    // // Act & Assert
    // assertThatThrownBy(() -> vehicleService.createVehicle(null))
    // .isInstanceOf(NullPointerException.class)
    // .hasMessageContaining("CreateVehicleDto cannot be null");
    //
    // verify(vehicleRepository, never()).save(any(Vehicle.class));
    // }

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("Should validate DTO fields before creating vehicle")
    // void shouldValidateDtoFields() {
    // // Arrange
    // CreateVehicleDto invalidDto = new CreateVehicleDto();
    // createVehicleDto.setPlateNo("");
    // createVehicleDto.setModel(null);
    // createVehicleDto.setSeats(-1);
    //
    // // Act & Assert
    // assertThatThrownBy(() -> vehicleService.createVehicle(invalidDto))
    // .isInstanceOf(IllegalArgumentException.class);
    //
    // verify(vehicleRepository, never()).save(any(Vehicle.class));
    // }
}
