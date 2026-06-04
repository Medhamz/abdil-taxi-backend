package com.abdil.taxi.repository;

import com.abdil.taxi.model.RechargeCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface RechargeCouponRepository extends JpaRepository<RechargeCoupon, Long> {
    Optional<RechargeCoupon> findByCodeAndStatus(String code, String status);

    @Modifying
    @Transactional
    @Query("UPDATE RechargeCoupon c SET c.status = 'USED', c.usedByUserId = :userId, c.usedAt = CURRENT_TIMESTAMP WHERE c.code = :code AND c.status = 'ACTIVE'")
    int useCoupon(String code, Long userId);
}