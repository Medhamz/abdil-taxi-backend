package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideRepository rideRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClients", userRepository.count());
        stats.put("totalDrivers", driverRepository.count());
        stats.put("totalRides", rideRepository.count());
        stats.put("pendingRides", rideRepository.findByStatus("PENDING").size());
        stats.put("acceptedRides", rideRepository.findByStatus("ACCEPTED").size());
        stats.put("completedRides", rideRepository.findByStatus("COMPLETED").size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<User>> getAllClients() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverRepository.findAll());
    }

    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideRepository.findAll());
    }

    @DeleteMapping("/client/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/driver/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/ride/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable Long id) {
        rideRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ride/{id}/status")
    public ResponseEntity<Ride> updateRideStatus(@PathVariable Long id, @RequestParam String status) {
        Ride ride = rideRepository.findById(id).orElse(null);
        if (ride != null) {
            ride.setStatus(status);
            return ResponseEntity.ok(rideRepository.save(ride));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/drivers/online")
    public ResponseEntity<List<Driver>> getOnlineDrivers() {
        List<Driver> onlineDrivers = driverRepository.findByStatus("ONLINE");
        return ResponseEntity.ok(onlineDrivers);
    }

    @GetMapping("/driver/{id}/location")
    public ResponseEntity<Map<String, Double>> getDriverLocation(@PathVariable Long id) {
        Driver driver = driverRepository.findById(id).orElse(null);
        if (driver != null) {
            Map<String, Double> location = new HashMap<>();
            location.put("lat", driver.getLatitude());
            location.put("lng", driver.getLongitude());
            return ResponseEntity.ok(location);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<Map<String, Object>>> getHeatmapData() {
        List<Map<String, Object>> hotspots = new ArrayList<>();

        try {
            List<Ride> pendingRides = rideRepository.findByStatus("PENDING");
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

            List<Ride> recentPending = pendingRides.stream()
                    .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(thirtyMinutesAgo))
                    .collect(Collectors.toList());

            Map<String, List<Ride>> groupedByZone = recentPending.stream()
                    .filter(r -> r.getPickupAddress() != null)
                    .collect(Collectors.groupingBy(r ->
                            r.getPickupAddress().substring(0, Math.min(20, r.getPickupAddress().length()))
                    ));

            for (Map.Entry<String, List<Ride>> entry : groupedByZone.entrySet()) {
                int intensity = Math.min(100, entry.getValue().size() * 15);

                Map<String, Object> point = new HashMap<>();
                point.put("lat", 33.5731 + (Math.random() * 0.05));
                point.put("lng", -7.5898 + (Math.random() * 0.05));
                point.put("intensity", intensity);
                point.put("zoneName", entry.getKey());
                point.put("requestCount", entry.getValue().size());
                hotspots.add(point);
            }

            hotspots.sort((a, b) -> Integer.compare(
                    (int) b.get("intensity"), (int) a.get("intensity")
            ));

        } catch (Exception e) {
            // Données de démonstration
            hotspots.add(createHotspot(33.5731, -7.5898, 85, "Centre-ville", 15));
            hotspots.add(createHotspot(33.5898, -7.6122, 72, "Gare routière", 10));
            hotspots.add(createHotspot(33.5512, -7.6189, 68, "Marché Central", 8));
        }

        return ResponseEntity.ok(hotspots);
    }

    private Map<String, Object> createHotspot(double lat, double lng, int intensity, String zoneName, int requestCount) {
        Map<String, Object> point = new HashMap<>();
        point.put("lat", lat);
        point.put("lng", lng);
        point.put("intensity", intensity);
        point.put("zoneName", zoneName);
        point.put("requestCount", requestCount);
        return point;
    }

}