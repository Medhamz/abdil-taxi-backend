package com.abdil.taxi.controller;

import com.abdil.taxi.model.ScheduledRide;
import com.abdil.taxi.repository.ScheduledRideRepository;
import com.abdil.taxi.service.TaxiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduled")
@CrossOrigin(origins = "*")
public class ScheduledRideController {

    @Autowired
    private ScheduledRideRepository scheduledRideRepository;

    @Autowired
    private TaxiService taxiService;

    @PostMapping("/create")
    public ResponseEntity<ScheduledRide> createScheduledRide(@RequestBody ScheduledRide request) {
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());

        // Calculer le prix
        var priceResponse = taxiService.calculatePrice(request.getDistance(), request.getRideType());
        request.setEstimatedPrice(priceResponse.getEstimatedPrice());

        ScheduledRide saved = scheduledRideRepository.save(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduledRide>> getUserScheduledRides(@PathVariable Long userId) {
        return ResponseEntity.ok(scheduledRideRepository.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<ScheduledRide>> getUpcomingScheduledRides(@PathVariable Long userId) {
        List<ScheduledRide> rides = scheduledRideRepository.findByUserIdAndStatus(userId, "PENDING");
        rides.removeIf(r -> r.getScheduledDateTime().isBefore(LocalDateTime.now()));
        return ResponseEntity.ok(rides);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelScheduledRide(@PathVariable Long id) {
        scheduledRideRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ScheduledRide>> getAllScheduledRides() {
        return ResponseEntity.ok(scheduledRideRepository.findAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ScheduledRide>> getPendingScheduledRides() {
        return ResponseEntity.ok(scheduledRideRepository.findByStatus("PENDING"));
    }

}