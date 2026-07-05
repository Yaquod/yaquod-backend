package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import com.yaquodorg.yaquod.dtos.admin.DashboardDto;
import com.yaquodorg.yaquod.dtos.admin.VehicleDto;
import com.yaquodorg.yaquod.entity.Payment;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.service.admin.DashboardService;
import com.yaquodorg.yaquod.service.payment.PaymentService;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin dashboard and management APIs")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final TripService tripService;
    private final VehicleService vehicleService;
    private final RequestService requestService;
    private final PaymentService paymentService;

    @Operation(summary = "Get dashboard statistics")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard() {
        DashboardDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(createSuccessResponse(stats));
    }

    @Operation(summary = "List all users with pagination and search")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search) {
        Sort sortOrder =
                direction.equalsIgnoreCase("desc")
                        ? Sort.by(sort).descending()
                        : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<User> users =
                search != null && !search.isBlank()
                        ? userService.searchUsers(search, pageable)
                        : userService.getAllUsers(pageable);
        return ResponseEntity.ok(createSuccessResponse(users));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(createSuccessResponse(user));
    }

    @Operation(summary = "Update user role")
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long userId, @RequestParam Role role) {
        User user = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(createSuccessResponse(user));
    }

    @Operation(summary = "Get trips for a specific user")
    @GetMapping("/users/{userId}/trips")
    public ResponseEntity<ApiResponse<List<Trip>>> getUserTrips(@PathVariable Long userId) {
        List<Trip> trips = tripService.getTripsByUserId(userId);
        return ResponseEntity.ok(createSuccessResponse(trips));
    }

    @Operation(summary = "Set user email verification status")
    @PatchMapping("/users/{userId}/verify")
    public ResponseEntity<ApiResponse<User>> updateUserVerified(
            @PathVariable Long userId, @RequestParam boolean verified) {
        User user = userService.updateEmailVerified(userId, verified);
        return ResponseEntity.ok(createSuccessResponse(user));
    }

    @Operation(summary = "List all vehicles")
    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getVehicles() {
        List<VehicleDto> vehicles =
                vehicleService.getVehicles().stream().map(VehicleDto::fromEntity).toList();
        return ResponseEntity.ok(createSuccessResponse(vehicles));
    }

    @Operation(summary = "Update vehicle status")
    @PatchMapping("/vehicles/{vehicleId}/status")
    public ResponseEntity<ApiResponse<VehicleDto>> updateVehicleStatus(
            @PathVariable Long vehicleId, @RequestParam VehicleStatus status) {
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);
        vehicleService.updateVehicleStatus(vehicleId, status);
        vehicle.setStatus(status);
        return ResponseEntity.ok(createSuccessResponse(VehicleDto.fromEntity(vehicle)));
    }

    @Operation(summary = "List all requests")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<Request>>> getRequests() {
        List<Request> requests = requestService.getRequests();
        return ResponseEntity.ok(createSuccessResponse(requests));
    }

    @Operation(summary = "List all payments")
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<Payment>>> getPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(createSuccessResponse(payments));
    }
}
