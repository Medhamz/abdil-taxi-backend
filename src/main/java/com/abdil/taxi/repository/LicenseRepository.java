package com.abdil.taxi.repository;

import com.abdil.taxi.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByLicenseKey(String licenseKey);

    List<License> findByUserIdAndAppType(Long userId, String appType);

    List<License> findByStatus(String status);

    List<License> findByLicenseType(String licenseType);

    @Query("SELECT l FROM License l WHERE l.status = 'ACTIVE' AND l.endDate < :now")
    List<License> findExpiredActiveLicenses(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(l) FROM License l WHERE l.status = 'ACTIVE'")
    long countActiveLicenses();

    @Query("SELECT l.licenseType, COUNT(l) FROM License l GROUP BY l.licenseType")
    List<Object[]> countByLicenseType();

    @Query("SELECT SUM(l.price) FROM License l WHERE l.status = 'ACTIVE'")
    Long sumTotalRevenue();

    Optional<License> findByUserIdAndAppTypeAndStatus(Long userId, String appType, String status);
}