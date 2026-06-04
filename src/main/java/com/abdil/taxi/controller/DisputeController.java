package com.abdil.taxi.controller;

import com.abdil.taxi.model.Dispute;
import com.abdil.taxi.model.Ride;
import com.abdil.taxi.repository.DisputeRepository;
import com.abdil.taxi.repository.RideRepository;
import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disputes")
@CrossOrigin(origins = "*")
public class DisputeController {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createDispute(@RequestBody Map<String, Object> request) {
        Long rideId = ((Number) request.get("rideId")).longValue();
        Long clientId = ((Number) request.get("clientId")).longValue();
        String reason = (String) request.get("reason");
        String description = (String) request.get("description");

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Course non trouvée"));
        }

        Dispute dispute = new Dispute();
        dispute.setRideId(rideId);
        dispute.setClientId(clientId);
        dispute.setDriverId(ride.getDriverId());
        dispute.setReason(reason);
        dispute.setDescription(description);

        Dispute saved = disputeRepository.save(dispute);

        // Notifier l'admin
        System.out.println("📢 Nouveau litige créé: ID=" + saved.getId() + " pour course " + rideId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("disputeId", saved.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeRepository.findAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Dispute>> getPendingDisputes() {
        return ResponseEntity.ok(disputeRepository.findByStatus("PENDING"));
    }

    @PutMapping("/resolve/{id}")
    public ResponseEntity<Dispute> resolveDispute(@PathVariable Long id,
                                                  @RequestParam String resolution,
                                                  @RequestParam Double refundAmount) {
        Dispute dispute = disputeRepository.findById(id).orElse(null);
        if (dispute == null) return ResponseEntity.notFound().build();

        dispute.setStatus("RESOLVED");
        dispute.setResolution(resolution);
        dispute.setRefundAmount(refundAmount);
        dispute.setResolvedAt(LocalDateTime.now());

        Dispute saved = disputeRepository.save(dispute);

        // Notifier le client
        notificationService.sendNotificationToClient(
                dispute.getClientId(),
                "✅ Litige résolu",
                "Votre litige a été résolu. " + resolution
        );

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<Dispute> rejectDispute(@PathVariable Long id, @RequestParam String reason) {
        Dispute dispute = disputeRepository.findById(id).orElse(null);
        if (dispute == null) return ResponseEntity.notFound().build();

        dispute.setStatus("REJECTED");
        dispute.setResolution(reason);
        dispute.setResolvedAt(LocalDateTime.now());

        return ResponseEntity.ok(disputeRepository.save(dispute));
    }
}