package com.abdil.taxi.repository;

import com.abdil.taxi.model.AdCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdCampaignRepository extends JpaRepository<AdCampaign, Long> {
    List<AdCampaign> findByClientId(Long clientId);
    List<AdCampaign> findByStatus(String status);
}