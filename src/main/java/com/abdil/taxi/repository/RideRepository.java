package com.abdil.taxi.repository;

import com.abdil.taxi.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByStatus(String status);
    List<Ride> findByClientPhone(String phone);
}