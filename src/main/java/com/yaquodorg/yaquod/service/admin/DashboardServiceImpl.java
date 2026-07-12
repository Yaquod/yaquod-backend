package com.yaquodorg.yaquod.service.admin;

import com.yaquodorg.yaquod.dtos.admin.DashboardDto;
import com.yaquodorg.yaquod.entity.PaymentStatus;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.service.payment.PaymentService;
import com.yaquodorg.yaquod.service.rating.RatingService;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserService userService;
    private final VehicleService vehicleService;
    private final TripService tripService;
    private final PaymentService paymentService;
    private final RequestService requestService;
    private final RatingService ratingService;

    private static final List<VehicleStatus> BUSY_VEHICLE_STATUSES =
            List.of(
                    VehicleStatus.PROCESSING,
                    VehicleStatus.ON_WAY,
                    VehicleStatus.WAITING_PASSENGER,
                    VehicleStatus.IN_USE);

    private static final List<VehicleStatus> UNAVAILABLE_VEHICLE_STATUSES =
            List.of(VehicleStatus.ON_HOLD, VehicleStatus.OUT_OF_SERVICE);

    private static final List<RequestStatus> FAILED_REQUEST_STATUSES =
            List.of(
                    RequestStatus.CANCELLED,
                    RequestStatus.TIMEOUT,
                    RequestStatus.FAILED,
                    RequestStatus.DECLINED);

    private static final List<TripStatus> PRE_TRIP_STATUSES =
            List.of(
                    TripStatus.INITIATED,
                    TripStatus.VEHICLE_ON_WAY,
                    TripStatus.VEHICLE_CLOSE,
                    TripStatus.ARRIVED_AT_PICKUP);

    private static final List<TripStatus> ACTIVE_TRIP_STATUSES =
            List.of(TripStatus.PASSENGER_ONBOARD, TripStatus.IN_PROGRESS);

    private static final List<TripStatus> COMPLETED_TRIP_STATUSES =
            List.of(TripStatus.ARRIVED_AT_DESTINATION, TripStatus.COMPLETED);

    private static final List<TripStatus> CANCELLED_TRIP_STATUSES =
            List.of(
                    TripStatus.PASSENGER_NO_SHOW,
                    TripStatus.CANCELLED_BY_PASSENGER,
                    TripStatus.CANCELLED_BY_SYSTEM);

    private static final List<TripStatus> ISSUE_TRIP_STATUSES =
            List.of(
                    TripStatus.EMERGENCY,
                    TripStatus.INCIDENT,
                    TripStatus.VEHICLE_ISSUE,
                    TripStatus.REFUNDED);

    @Override
    public DashboardDto getDashboardStats() {
        log.info("Fetching dashboard statistics");

        long totalUsers = userService.countUsers();
        long totalAdmins = userService.countUsersByRole(Role.ADMIN);
        long totalClients = userService.countUsersByRole(Role.CLIENT);
        long totalVehicles = vehicleService.countVehicles();
        long idleVehicles = vehicleService.countVehiclesByStatusIn(List.of(VehicleStatus.IDLE));
        long busyVehicles = vehicleService.countVehiclesByStatusIn(BUSY_VEHICLE_STATUSES);
        long unavailableVehicles =
                vehicleService.countVehiclesByStatusIn(UNAVAILABLE_VEHICLE_STATUSES);
        long totalTrips = tripService.countTrips();
        long preTripTrips = tripService.countTripsByStatusIn(PRE_TRIP_STATUSES);
        long activeTrips = tripService.countTripsByStatusIn(ACTIVE_TRIP_STATUSES);
        long completedTrips = tripService.countTripsByStatusIn(COMPLETED_TRIP_STATUSES);
        long cancelledTrips = tripService.countTripsByStatusIn(CANCELLED_TRIP_STATUSES);
        long issueTrips = tripService.countTripsByStatusIn(ISSUE_TRIP_STATUSES);
        long pendingRequests =
                requestService.countRequestsByStatusIn(List.of(RequestStatus.PENDING));
        long acceptedRequests =
                requestService.countRequestsByStatusIn(List.of(RequestStatus.ACCEPTED));
        long completedRequests =
                requestService.countRequestsByStatusIn(List.of(RequestStatus.COMPLETED));
        long failedRequests = requestService.countRequestsByStatusIn(FAILED_REQUEST_STATUSES);
        long totalPayments = paymentService.countPaymentsByStatus(PaymentStatus.PAID);
        double totalRevenue = paymentService.sumAmountByStatus(PaymentStatus.PAID);
        long totalRatings = ratingService.countRatings();
        double avgRating = ratingService.averageRating();

        return DashboardDto.builder()
                .totalUsers(totalUsers)
                .totalAdmins(totalAdmins)
                .totalClients(totalClients)
                .totalVehicles(totalVehicles)
                .idleVehicles(idleVehicles)
                .busyVehicles(busyVehicles)
                .unavailableVehicles(unavailableVehicles)
                .totalTrips(totalTrips)
                .preTripTrips(preTripTrips)
                .activeTrips(activeTrips)
                .completedTrips(completedTrips)
                .cancelledTrips(cancelledTrips)
                .issueTrips(issueTrips)
                .pendingRequests(pendingRequests)
                .acceptedRequests(acceptedRequests)
                .completedRequests(completedRequests)
                .failedRequests(failedRequests)
                .totalPayments(totalPayments)
                .totalRevenue(totalRevenue)
                .totalRatings(totalRatings)
                .avgRating(avgRating)
                .build();
    }
}
