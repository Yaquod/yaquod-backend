package com.yaquodorg.yaquod.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yaquodorg.yaquod.entity.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
}
