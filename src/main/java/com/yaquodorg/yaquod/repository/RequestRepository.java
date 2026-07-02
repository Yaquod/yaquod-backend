package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {
    long countByStatusIn(Collection<RequestStatus> statuses);
}
