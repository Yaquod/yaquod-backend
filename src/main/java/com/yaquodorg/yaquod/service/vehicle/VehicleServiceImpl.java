package com.yaquodorg.yaquod.service.vehicle;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Vehicle createVehicle(CreateVehicleDto createVehicleDto) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findByVehicleUUID(createVehicleDto.getVehicleUUID());
        if (vehicleOptional.isPresent()) {
            throw new RuntimeException("Vehicle already exists!");
        }

        Vehicle vehicle = new Vehicle();
        buildVehicleFromDto(vehicle, createVehicleDto);

        return vehicleRepository.save(vehicle);
    }

    @Override
    public List<Vehicle> getVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Vehicle not found!"));
    }

    @Override
    public Optional<Vehicle> getVehicleByUUID(String vehicleUUID) {
        return vehicleRepository.findByVehicleUUID(vehicleUUID);
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(CreateVehicleDto createVehicleDto) {
        Vehicle vehicle = vehicleRepository.findByVehicleUUID(createVehicleDto.getVehicleUUID())
                .orElseThrow(() -> new RuntimeException(
                        "Vehicle with UUID " + createVehicleDto.getVehicleUUID() + " not found!"));

        buildVehicleFromDto(vehicle, createVehicleDto);
        return vehicle;
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    private Vehicle buildVehicleFromDto(Vehicle vehicle, CreateVehicleDto dto) {
        vehicle.setVehicleUUID(dto.getVehicleUUID());
        vehicle.setPlateNo(dto.getPlateNo());
        vehicle.setColor(dto.getColor());
        vehicle.setCarCompany(dto.getCarCompany());
        vehicle.setModel(dto.getModel());
        vehicle.setSeats(dto.getSeats());
        return vehicle;
    }
}
