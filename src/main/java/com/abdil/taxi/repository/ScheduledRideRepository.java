package com.abdil.taxi.repository;

import com.abdil.taxi.model.ScheduledRide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledRideRepository extends JpaRepository<ScheduledRide, Long> {
    List<ScheduledRide> findByUserId(Long userId);

    List<ScheduledRide> findByStatus(String status);
    List<ScheduledRide> findByStatusAndScheduledDateTimeBefore(String status, LocalDateTime dateTime);
    List<ScheduledRide> findByUserIdAndStatus(Long userId, String status);

    List<ScheduledRide> findByStatusAndScheduledDateTimeBetween(String status, LocalDateTime start, LocalDateTime end);
}