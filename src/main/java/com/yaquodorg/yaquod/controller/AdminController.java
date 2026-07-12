package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import com.yaquodorg.yaquod.dtos.admin.DashboardDto;
import com.yaquodorg.yaquod.dtos.admin.PaymentDto;
import com.yaquodorg.yaquod.dtos.admin.RequestDto;
import com.yaquodorg.yaquod.dtos.admin.TripDto;
import com.yaquodorg.yaquod.dtos.admin.VehicleDto;
import com.yaquodorg.yaquod.dtos.admin.VehicleDto;
import com.yaquodorg.yaquod.dtos.request.RequestDto;
import com.yaquodorg.yaquod.entity.Payment;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.RatingResponse;
import com.yaquodorg.yaquod.service.admin.DashboardService;
import com.yaquodorg.yaquod.service.payment.PaymentService;
import com.yaquodorg.yaquod.service.rating.RatingService;
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
    private final RatingService ratingService;

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
    public ResponseEntity<ApiResponse<List<TripDto>>> getUserTrips(@PathVariable Long userId) {
        List<TripDto> trips =
                tripService.getTripsByUserId(userId).stream().map(TripDto::fromEntity).toList();
        return ResponseEntity.ok(createSuccessResponse(trips));
    }

    @Operation(summary = "List all trips")
    @GetMapping("/trips")
    public ResponseEntity<ApiResponse<List<TripDto>>> getTrips() {
        List<TripDto> trips =
                tripService.getAllTripsWithAssociations().stream()
                        .map(TripDto::fromEntity)
                        .toList();
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
        vehicleService.updateVehicleStatus(vehicleId, status);
        Vehicle updatedVehicle = vehicleService.getVehicle(vehicleId);
        return ResponseEntity.ok(createSuccessResponse(VehicleDto.fromEntity(updatedVehicle)));
    }

    @Operation(summary = "List all requests")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RequestDto>>> getRequests() {
        List<RequestDto> requests =
                requestService.getRequestsWithTripAndVehicle().stream()
                        .map(RequestDto::fromEntity)
                        .toList();
        return ResponseEntity.ok(createSuccessResponse(requests));
    }

    @Operation(summary = "List all payments")
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPayments() {
        List<PaymentDto> payments =
                paymentService.getAllPayments().stream().map(PaymentDto::fromEntity).toList();
        return ResponseEntity.ok(createSuccessResponse(payments));
    }

    @Operation(summary = "List all ratings")
    @GetMapping("/ratings")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRatings() {
        List<RatingResponse> ratings =
                ratingService.getAllRatings().stream()
                        .map(RatingService::toRatingResponse)
                        .toList();
        return ResponseEntity.ok(createSuccessResponse(ratings));
    }
}
