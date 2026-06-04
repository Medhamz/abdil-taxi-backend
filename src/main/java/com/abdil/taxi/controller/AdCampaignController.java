package com.abdil.taxi.controller;

import com.abdil.taxi.model.AdCampaign;
import com.abdil.taxi.repository.AdCampaignRepository;
import com.abdil.taxi.service.WalletService;
import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/advertising")
@CrossOrigin(origins = "*")
public class AdCampaignController {

    @Autowired
    private AdCampaignRepository adRepo;

    @Autowired
    private WalletService walletService;

    @Autowired
    private NotificationService notificationService;

    // Tarifs
    private static final Map<String, Double> PRICES = Map.of(
            "1 MONTH", 10000.0,
            "6 MONTHS", 50000.0,
            "1 YEAR", 100000.0
    );

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAd(@RequestBody AdCampaign ad) {
        Map<String, Object> response = new HashMap<>();
        ad.setCreatedAt(LocalDateTime.now());
        ad.setStatus("PENDING");
        ad.setPrice(PRICES.get(ad.getDuration()));
        if (ad.getPrice() == null) {
            response.put("success", false);
            response.put("message", "Durée invalide");
            return ResponseEntity.badRequest().body(response);
        }
        AdCampaign saved = adRepo.save(ad);
        response.put("success", true);
        response.put("campaignId", saved.getId());
        response.put("price", saved.getPrice());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay/{campaignId}")
    public ResponseEntity<Map<String, Object>> payForAd(
            @PathVariable Long campaignId,
            @RequestParam Long clientId,
            @RequestParam String paymentMethod) {
        Map<String, Object> response = new HashMap<>();
        AdCampaign ad = adRepo.findById(campaignId).orElse(null);
        if (ad == null || !ad.getClientId().equals(clientId)) {
            response.put("success", false);
            response.put("message", "Campagne non trouvée");
            return ResponseEntity.badRequest().body(response);
        }
        if (!"PENDING".equals(ad.getStatus())) {
            response.put("success", false);
            response.put("message", "Campagne déjà traitée");
            return ResponseEntity.badRequest().body(response);
        }

        if ("WALLET".equals(paymentMethod)) {
            boolean debited = walletService.debitWallet(clientId, ad.getPrice(), "AD_" + campaignId);
            if (!debited) {
                response.put("success", false);
                response.put("message", "Solde insuffisant");
                return ResponseEntity.badRequest().body(response);
            }
            ad.setStatus("PAID");
            ad.setPaidAt(LocalDateTime.now());
            adRepo.save(ad);
            // Génération d'un reçu
            String receiptId = UUID.randomUUID().toString();
            notificationService.sendNotificationToUser(clientId,
                    "💰 Paiement publicité accepté",
                    "Votre paiement de " + ad.getPrice() + " FCFA pour la campagne #" + campaignId + " a été reçu. Un reçu vous sera envoyé.");
            response.put("success", true);
            response.put("message", "Paiement effectué. Votre publicité sera imprimée sous 7 jours.");
            response.put("receiptId", receiptId);
        } else if ("CASH".equals(paymentMethod)) {
            ad.setStatus("PENDING_ADMIN");
            adRepo.save(ad);
            response.put("success", true);
            response.put("message", "Demande enregistrée. Veuillez payer en espèces à l'admin pour validation.");
        } else {
            response.put("success", false);
            response.put("message", "Mode de paiement non supporté");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<AdCampaign>> getAllForAdmin() {
        return ResponseEntity.ok(adRepo.findAll());
    }

    @PutMapping("/admin/validate/{campaignId}")
    public ResponseEntity<Map<String, Object>> adminValidateCashPayment(
            @PathVariable Long campaignId,
            @RequestParam String adminNotes) {
        Map<String, Object> response = new HashMap<>();
        AdCampaign ad = adRepo.findById(campaignId).orElse(null);
        if (ad == null || !"PENDING_ADMIN".equals(ad.getStatus())) {
            response.put("success", false);
            response.put("message", "Campagne non trouvée ou déjà traitée");
            return ResponseEntity.badRequest().body(response);
        }
        ad.setStatus("VALIDATED_BY_ADMIN");
        ad.setAdminNotes(adminNotes);
        ad.setPaidAt(LocalDateTime.now());
        adRepo.save(ad);
        notificationService.sendNotificationToUser(ad.getClientId(),
                "✅ Publicité validée (espèces)",
                "Votre paiement en espèces a été confirmé par l'admin. Votre publicité sera imprimée sous 7 jours.");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AdCampaign>> getClientAds(@PathVariable Long clientId) {
        return ResponseEntity.ok(adRepo.findByClientId(clientId));
    }
}