package com.abdil.taxi.controller;

import com.abdil.taxi.model.AdCampaign;
import com.abdil.taxi.repository.AdCampaignRepository;
import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/advertising")
@CrossOrigin(origins = "*")
public class AdminAdvertisingController {

    @Autowired
    private AdCampaignRepository adCampaignRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Liste toutes les demandes de publicité (pour l'admin)
     * GET /api/admin/advertising/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<AdCampaign>> getAllAdvertising() {
        return ResponseEntity.ok(adCampaignRepository.findAll());
    }

    /**
     * Valide un paiement en espèces (admin)
     * PUT /api/admin/advertising/admin/validate/{campaignId}
     */
    @PutMapping("/admin/validate/{campaignId}")
    public ResponseEntity<Map<String, Object>> validateCashPayment(
            @PathVariable Long campaignId,
            @RequestParam(required = false) String adminNotes) {

        Map<String, Object> response = new HashMap<>();
        AdCampaign ad = adCampaignRepository.findById(campaignId).orElse(null);

        if (ad == null) {
            response.put("success", false);
            response.put("message", "Campagne non trouvée");
            return ResponseEntity.badRequest().body(response);
        }

        if (!"PENDING_ADMIN".equals(ad.getStatus())) {
            response.put("success", false);
            response.put("message", "Cette campagne n'est pas en attente de validation admin");
            return ResponseEntity.badRequest().body(response);
        }

        ad.setStatus("VALIDATED_BY_ADMIN");
        ad.setAdminNotes(adminNotes);
        ad.setValidatedAt(LocalDateTime.now());
        adCampaignRepository.save(ad);

        // Envoyer une notification au client
        try {
            notificationService.sendNotificationToUser(ad.getClientId(),
                    "✅ Publicité validée",
                    "Votre paiement en espèces a été confirmé. Votre publicité sera imprimée et posée sur les taxis sous 7 jours.");
        } catch (Exception e) {
            System.err.println("Erreur envoi notification: " + e.getMessage());
        }

        response.put("success", true);
        response.put("message", "Paiement espèces validé, la publicité sera imprimée");
        return ResponseEntity.ok(response);
    }
}