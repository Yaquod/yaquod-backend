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
        vehicle.setVehicleUUID(createVehicleDto.getVehicleUUID());
        vehicle.setPlateNo(createVehicleDto.getPlateNo());
        vehicle.setColor(createVehicleDto.getColor());
        vehicle.setCarCompany(createVehicleDto.getCarCompany());
        vehicle.setModel(createVehicleDto.getModel());
        vehicle.setSeats(createVehicleDto.getSeats());

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
        Optional<Vehicle> vehicleOptional = vehicleRepository.findByVehicleUUID(createVehicleDto.getVehicleUUID());
        if (vehicleOptional.isEmpty()) {
            throw new RuntimeException("Vehicle not found!");
        }

        Vehicle vehicle = vehicleOptional.get();
        vehicle.setVehicleUUID(createVehicleDto.getVehicleUUID());
        vehicle.setPlateNo(createVehicleDto.getPlateNo());
        vehicle.setColor(createVehicleDto.getColor());
        vehicle.setCarCompany(createVehicleDto.getCarCompany());
        vehicle.setModel(createVehicleDto.getModel());
        vehicle.setSeats(createVehicleDto.getSeats());

        return vehicle;
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}
