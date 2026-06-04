package com.abdil.taxi.repository;

import com.abdil.taxi.model.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByStatus(String status);
    List<Dispute> findByClientId(Long clientId);
    List<Dispute> findByDriverId(Long driverId);
}