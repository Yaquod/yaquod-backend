package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequestRepository extends JpaRepository<Request, Long> {
    long countByStatusIn(Collection<RequestStatus> statuses);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.trip t LEFT JOIN FETCH t.vehicle")
    List<Request> findAllWithTripAndVehicle();
}
