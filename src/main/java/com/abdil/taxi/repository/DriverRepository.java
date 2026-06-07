package com.abdil.taxi.repository;

import com.abdil.taxi.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByEmail(String email);

    Optional<Driver> findByPhone(String phone);

    List<Driver> findByStatus(String status);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<Driver> findByStatusAndIsFemaleOnly(String status, Boolean isFemaleOnly);

    // Récupérer tous les chauffeurs en ligne (ONLINE ou ON_TRIP)
    @Query("SELECT d FROM Driver d WHERE d.status = 'ONLINE' OR d.status = 'ON_TRIP'")
    List<Driver> findOnlineDrivers();

    // Récupérer les chauffeurs avec position valide (non nulle)
    @Query("SELECT d FROM Driver d WHERE d.latitude IS NOT NULL AND d.longitude IS NOT NULL AND d.latitude != 0 AND d.longitude != 0")
    List<Driver> findDriversWithValidLocation();

    // Compter les chauffeurs en ligne
    @Query("SELECT COUNT(d) FROM Driver d WHERE d.status = 'ONLINE' OR d.status = 'ON_TRIP'")
    long countOnlineDrivers();

    // Récupérer les chauffeurs par statut et avec position
    @Query("SELECT d FROM Driver d WHERE d.status = :status AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL")
    List<Driver> findByStatusAndLocationNotNull(@Param("status") String status);
}