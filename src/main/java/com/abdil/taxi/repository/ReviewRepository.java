package com.abdil.taxi.repository;

import com.abdil.taxi.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByRideId(Long rideId);

    List<Review> findByDriverId(Long driverId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.driverId = :driverId")
    Double getAverageRatingByDriverId(@Param("driverId") Long driverId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.driverId = :driverId")
    Long getRatingCountByDriverId(@Param("driverId") Long driverId);

    boolean existsByRideId(Long rideId);
}