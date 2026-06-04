package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.*;
import com.abdil.taxi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/driver")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping("/register")
    public ResponseEntity<DriverAuthResponse> register(@RequestBody DriverRegisterRequest request) {
        try {
            System.out.println("=== INSCRIPTION CHAUFFEUR ===");
            System.out.println("Nom: " + request.getFullName());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Téléphone: " + request.getPhone());
            System.out.println("Véhicule: " + request.getVehicleType());
            System.out.println("Plaque: " + request.getLicensePlate());

            if (driverRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Email déjà utilisé", false, null));
            }

            if (driverRepository.existsByPhone(request.getPhone())) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Téléphone déjà utilisé", false, null));
            }

            Driver driver = new Driver();
            driver.setFullName(request.getFullName());
            driver.setEmail(request.getEmail());
            driver.setPhone(request.getPhone());
            driver.setPassword(request.getPassword());
            driver.setVehicleType(request.getVehicleType());
            driver.setLicensePlate(request.getLicensePlate());
            driver.setStatus("OFFLINE");

            Driver savedDriver = driverRepository.save(driver);
            System.out.println("✅ Chauffeur créé avec ID: " + savedDriver.getId());

            return ResponseEntity.ok(new DriverAuthResponse(null, "Inscription réussie", true, savedDriver));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DriverAuthResponse(null, "Erreur: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<DriverAuthResponse> login(@RequestBody DriverAuthRequest request) {
        try {
            System.out.println("=== TENTATIVE DE CONNEXION CHAUFFEUR ===");
            System.out.println("Email: " + request.getEmail());

            Driver driver = driverRepository.findByEmail(request.getEmail()).orElse(null);
            if (driver == null) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Email ou mot de passe incorrect", false, null));
            }

            if (!driver.getPassword().equals(request.getPassword())) {
                return ResponseEntity.badRequest().body(new DriverAuthResponse(null, "Email ou mot de passe incorrect", false, null));
            }

            System.out.println("✅ Connexion réussie pour: " + driver.getEmail());
            System.out.println("🔑 Driver ID: " + driver.getId());

            return ResponseEntity.ok(new DriverAuthResponse(null, "Connexion réussie", true, driver));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DriverAuthResponse(null, "Erreur: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/location")
    public ResponseEntity<Driver> updateLocation(@RequestParam Long driverId,
                                                 @RequestParam double lat,
                                                 @RequestParam double lng) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }
        driver.setLatitude(lat);
        driver.setLongitude(lng);
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @PostMapping("/status")
    public ResponseEntity<Driver> updateStatus(@RequestParam Long driverId, @RequestParam String status) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }
        driver.setStatus(status);
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @GetMapping("/rides/pending")
    public ResponseEntity<List<Ride>> getPendingRides() {
        List<Ride> pendingRides = rideRepository.findByStatus("PENDING");
        return ResponseEntity.ok(pendingRides);
    }

    @PutMapping("/ride/{rideId}/accept")
    public ResponseEntity<Ride> acceptRide(@PathVariable Long rideId, @RequestParam Long driverId) {
        System.out.println("=== ACCEPTATION COURSE ===");
        System.out.println("rideId: " + rideId);
        System.out.println("driverId: " + driverId);

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            System.out.println("❌ Course non trouvée");
            return ResponseEntity.notFound().build();
        }

        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            System.out.println("❌ Chauffeur non trouvé");
            return ResponseEntity.notFound().build();
        }

        ride.setStatus("ACCEPTED");
        ride.setDriverName(driver.getFullName());
        ride.setDriverId(driverId);
        Ride savedRide = rideRepository.save(ride);

        System.out.println("✅ Course acceptée: ID=" + savedRide.getId());

        try {
            notificationService.sendNotificationToClient(
                    ride.getUserId(),
                    "✅ Course acceptée",
                    "Votre chauffeur " + driver.getFullName() + " a accepté votre course et arrive bientôt!"
            );
            System.out.println("📱 Notification envoyée au client " + ride.getUserId());
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification client: " + e.getMessage());
        }

        return ResponseEntity.ok(savedRide);
    }

    @PutMapping("/ride/{rideId}/status")
    public ResponseEntity<Ride> updateRideStatus(@PathVariable Long rideId, @RequestParam String status) {
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }
        ride.setStatus(status);
        if ("COMPLETED".equals(status)) {
            ride.setCompletedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(rideRepository.save(ride));
    }

    @GetMapping("/driver/{driverId}/rides")
    public ResponseEntity<List<Ride>> getDriverRides(@PathVariable Long driverId) {
        List<Ride> rides = rideRepository.findAll().stream()
                .filter(r -> r.getDriverName() != null)
                .toList();
        return ResponseEntity.ok(rides);
    }

    // ✅ MÉTHODE MODIFIÉE POUR RENVOYER LE paymentMethod
    @GetMapping("/ride/active")
    public ResponseEntity<Map<String, Object>> getActiveRideForDriver(@RequestParam Long driverId) {
        List<Ride> activeRides = rideRepository.findAll().stream()
                .filter(r -> r.getDriverId() != null && r.getDriverId().equals(driverId))
                .filter(r -> r.getStatus().equals("ACCEPTED") || r.getStatus().equals("STARTED"))
                .sorted(Comparator.comparing(Ride::getCreatedAt).reversed())
                .toList();

        if (activeRides.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Ride activeRide = activeRides.get(0);
        System.out.println("🚖 Course active chauffeur: ID=" + activeRide.getId());
        System.out.println("💰 Mode de paiement: " + activeRide.getPaymentMethod());

        // ✅ Retourner un Map avec toutes les informations + paymentMethod
        Map<String, Object> response = new HashMap<>();
        response.put("id", activeRide.getId());
        response.put("userId", activeRide.getUserId());
        response.put("driverId", activeRide.getDriverId());
        response.put("driverName", activeRide.getDriverName());
        response.put("pickupAddress", activeRide.getPickupAddress());
        response.put("destinationAddress", activeRide.getDestinationAddress());
        response.put("status", activeRide.getStatus());
        response.put("estimatedPrice", activeRide.getEstimatedPrice());
        response.put("distance", activeRide.getDistance());
        response.put("createdAt", activeRide.getCreatedAt().toString());
        response.put("paymentMethod", activeRide.getPaymentMethod() != null ? activeRide.getPaymentMethod() : "CASH");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{driverId}")
    public ResponseEntity<List<Ride>> getDriverHistory(@PathVariable Long driverId) {
        System.out.println("=== HISTORIQUE CHAUFFEUR ID: " + driverId);

        List<Ride> history = rideRepository.findAll().stream()
                .filter(r -> r.getDriverId() != null && r.getDriverId().equals(driverId))
                .filter(r -> r.getStatus().equals("ACCEPTED") || r.getStatus().equals("COMPLETED"))
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .toList();

        System.out.println("📜 " + history.size() + " courses trouvées");
        return ResponseEntity.ok(history);
    }

    // ==================== STATISTIQUES TABLEAU DE BORD ====================

    @GetMapping("/dashboard/stats/{driverId}")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@PathVariable Long driverId) {
        System.out.println("=== STATISTIQUES CHAUFFEUR ID: " + driverId);

        List<Ride> allRides = rideRepository.findAll().stream()
                .filter(r -> r.getDriverId() != null && r.getDriverId().equals(driverId))
                .collect(Collectors.toList());

        List<Ride> completedRides = allRides.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .collect(Collectors.toList());

        List<Ride> acceptedRides = allRides.stream()
                .filter(r -> "ACCEPTED".equals(r.getStatus()) || "STARTED".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus()))
                .collect(Collectors.toList());

        double totalRevenue = completedRides.stream()
                .mapToDouble(Ride::getEstimatedPrice)
                .sum();

        Driver driver = driverRepository.findById(driverId).orElse(null);
        double averageRating = driver != null && driver.getRating() != null ? driver.getRating() : 0.0;
        long ratingCount = driver != null && driver.getRatingCount() != null ? driver.getRatingCount() : 0L;

        Map<String, Object> monthlyStats = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);

        for (int i = 5; i >= 0; i--) {
            LocalDateTime startOfMonth = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            final LocalDateTime start = startOfMonth;
            final LocalDateTime end = endOfMonth;

            double monthRevenue = completedRides.stream()
                    .filter(r -> r.getCompletedAt() != null)
                    .filter(r -> !r.getCompletedAt().isBefore(start) && !r.getCompletedAt().isAfter(end))
                    .mapToDouble(Ride::getEstimatedPrice)
                    .sum();

            long monthCount = completedRides.stream()
                    .filter(r -> r.getCompletedAt() != null)
                    .filter(r -> !r.getCompletedAt().isBefore(start) && !r.getCompletedAt().isAfter(end))
                    .count();

            String monthName = startOfMonth.format(monthFormatter);
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("revenue", monthRevenue);
            monthData.put("count", monthCount);
            monthlyStats.put(monthName, monthData);
        }

        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("PENDING", allRides.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        statusStats.put("ACCEPTED", allRides.stream().filter(r -> "ACCEPTED".equals(r.getStatus())).count());
        statusStats.put("STARTED", allRides.stream().filter(r -> "STARTED".equals(r.getStatus())).count());
        statusStats.put("COMPLETED", (long) completedRides.size());
        statusStats.put("CANCELLED", allRides.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count());

        Map<String, Long> dailyStats = new LinkedHashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH);

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1);

            final LocalDateTime start = startOfDay;
            final LocalDateTime end = endOfDay;

            long dayCount = completedRides.stream()
                    .filter(r -> r.getCompletedAt() != null)
                    .filter(r -> !r.getCompletedAt().isBefore(start) && !r.getCompletedAt().isAfter(end))
                    .count();

            String dayName = date.format(dayFormatter);
            dailyStats.put(dayName, dayCount);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalRides", allRides.size());
        response.put("completedRides", completedRides.size());
        response.put("acceptedRides", acceptedRides.size());
        response.put("totalRevenue", totalRevenue);
        response.put("averageRating", averageRating);
        response.put("ratingCount", ratingCount);
        response.put("monthlyStats", monthlyStats);
        response.put("statusStats", statusStats);
        response.put("dailyStats", dailyStats);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/history/{rideId}")
    public ResponseEntity<Void> deleteRideFromHistory(@PathVariable Long rideId) {
        System.out.println("=== SUPPRESSION COURSE ID: " + rideId);
        if (rideRepository.existsById(rideId)) {
            rideRepository.deleteById(rideId);
            System.out.println("✅ Course supprimée");
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/history/batch")
    public ResponseEntity<Void> deleteMultipleRides(@RequestBody List<Long> rideIds) {
        System.out.println("=== SUPPRESSION MULTIPLE: " + rideIds.size() + " courses");
        rideRepository.deleteAllById(rideIds);
        System.out.println("✅ Courses supprimées");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/location/{driverId}")
    public ResponseEntity<Map<String, Double>> getDriverLocation(@PathVariable Long driverId) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Double> location = new HashMap<>();
        location.put("latitude", driver.getLatitude() != null ? driver.getLatitude() : 0.0);
        location.put("longitude", driver.getLongitude() != null ? driver.getLongitude() : 0.0);
        return ResponseEntity.ok(location);
    }

    @PutMapping("/ride/{rideId}/cancel")
    public ResponseEntity<?> cancelRideByDriver(@PathVariable Long rideId,
                                                @RequestParam Long driverId,
                                                @RequestParam(required = false) String reason) {
        System.out.println("=== ANNULATION COURSE PAR CHAUFFEUR ===");
        System.out.println("rideId: " + rideId);
        System.out.println("driverId: " + driverId);
        System.out.println("Raison: " + reason);

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }

        if (ride.getDriverId() == null || !ride.getDriverId().equals(driverId)) {
            return ResponseEntity.badRequest().body("Cette course ne vous est pas assignée");
        }

        if (!ride.getStatus().equals("ACCEPTED") && !ride.getStatus().equals("STARTED")) {
            return ResponseEntity.badRequest().body("Impossible d'annuler une course non acceptée");
        }

        ride.setStatus("CANCELLED");
        ride.setCancellationReason(reason != null ? reason : "Annulé par le chauffeur");

        Ride cancelledRide = rideRepository.save(ride);
        System.out.println("✅ Course annulée par chauffeur: ID=" + cancelledRide.getId());

        try {
            notificationService.sendNotificationToClient(
                    ride.getUserId(),
                    "❌ Course annulée",
                    "Votre chauffeur a annulé la course. Raison: " + (reason != null ? reason : "Non spécifiée")
            );
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi notification: " + e.getMessage());
        }

        return ResponseEntity.ok(cancelledRide);
    }

    // ==================== HEATMAP POUR CHAUFFEUR ====================

    @GetMapping("/heatmap")
    public ResponseEntity<List<Map<String, Object>>> getHeatmapData() {
        System.out.println("=== RÉCUPÉRATION HEATMAP ===");
        List<Map<String, Object>> hotspots = new ArrayList<>();

        try {
            List<Ride> pendingRides = rideRepository.findByStatus("PENDING");
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

            List<Ride> recentPending = pendingRides.stream()
                    .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(thirtyMinutesAgo))
                    .collect(Collectors.toList());

            Map<String, List<Ride>> groupedByZone = recentPending.stream()
                    .collect(Collectors.groupingBy(r ->
                            r.getPickupAddress() != null ?
                                    r.getPickupAddress().substring(0, Math.min(20, r.getPickupAddress().length())) :
                                    "unknown"
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

            System.out.println("🔥 " + hotspots.size() + " zones de chaleur trouvées");

        } catch (Exception e) {
            System.err.println("Erreur heatmap: " + e.getMessage());
            hotspots.add(createDemoHotspot(33.5731, -7.5898, 85, "Centre-ville", 15));
            hotspots.add(createDemoHotspot(33.5898, -7.6122, 72, "Gare routière", 10));
            hotspots.add(createDemoHotspot(33.5512, -7.6189, 68, "Marché Central", 8));
            hotspots.add(createDemoHotspot(33.5625, -7.5780, 45, "Quartier Maârif", 6));
            hotspots.add(createDemoHotspot(33.5998, -7.6225, 38, "Aéroport", 5));
        }

        return ResponseEntity.ok(hotspots);
    }

    @GetMapping("/status/{driverId}")
    public ResponseEntity<Driver> getDriverStatus(@PathVariable Long driverId) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/pause")
    public ResponseEntity<Driver> setPause(@RequestParam Long driverId,
                                           @RequestParam String reason,
                                           @RequestParam Boolean isPause) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        driver.setIsOnPause(isPause);
        if (isPause) {
            driver.setPauseReason(reason);
            driver.setPauseStartTime(LocalDateTime.now());
            driver.setStatus("OFFLINE");
        } else {
            driver.setPauseReason(null);
            driver.setPauseStartTime(null);
            driver.setStatus("ONLINE");
        }

        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers(@RequestParam(required = false) Boolean femaleOnly) {
        List<Driver> drivers = driverRepository.findByStatus("ONLINE");

        if (femaleOnly != null && femaleOnly) {
            drivers = drivers.stream()
                    .filter(d -> d.getIsFemaleOnly() != null && d.getIsFemaleOnly())
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(drivers);
    }

    @PostMapping("/female-only")
    public ResponseEntity<Driver> updateFemaleOnlyStatus(@RequestParam Long driverId, @RequestParam Boolean enabled) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        driver.setIsFemaleOnly(enabled);
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @GetMapping("/female-only/available")
    public ResponseEntity<List<Driver>> getFemaleDrivers() {
        List<Driver> femaleDrivers = driverRepository.findByStatusAndIsFemaleOnly("ONLINE", true);
        return ResponseEntity.ok(femaleDrivers);
    }

    private Map<String, Object> createDemoHotspot(double lat, double lng, int intensity, String zoneName, int requestCount) {
        Map<String, Object> point = new HashMap<>();
        point.put("lat", lat);
        point.put("lng", lng);
        point.put("intensity", intensity);
        point.put("zoneName", zoneName);
        point.put("requestCount", requestCount);
        return point;
    }

    // ✅ CONFIRMATION PAIEMENT ESPÈCES PAR CHAUFFEUR
    @PutMapping("/ride/{rideId}/confirm-cash-payment")
    public ResponseEntity<Map<String, Object>> confirmCashPayment(
            @PathVariable Long rideId,
            @RequestParam Long driverId) {

        System.out.println("=== CONFIRMATION PAIEMENT ESPÈCES ===");
        System.out.println("rideId: " + rideId);
        System.out.println("driverId: " + driverId);

        Map<String, Object> response = new HashMap<>();

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            response.put("success", false);
            response.put("message", "Course non trouvée");
            return ResponseEntity.badRequest().body(response);
        }

        if (ride.getDriverId() == null || !ride.getDriverId().equals(driverId)) {
            response.put("success", false);
            response.put("message", "Vous n'êtes pas le chauffeur de cette course");
            return ResponseEntity.badRequest().body(response);
        }

        if (ride.getPaymentMethod() == null || !"CASH".equals(ride.getPaymentMethod())) {
            response.put("success", false);
            response.put("message", "Cette course n'est pas en paiement espèces");
            return ResponseEntity.badRequest().body(response);
        }

        if ("COMPLETED".equals(ride.getStatus())) {
            response.put("success", false);
            response.put("message", "Cette course est déjà terminée");
            return ResponseEntity.badRequest().body(response);
        }

        if (!"ACCEPTED".equals(ride.getStatus()) && !"STARTED".equals(ride.getStatus())) {
            response.put("success", false);
            response.put("message", "La course n'a pas encore commencé");
            return ResponseEntity.badRequest().body(response);
        }

        ride.setStatus("COMPLETED");
        ride.setCompletedAt(LocalDateTime.now());
        rideRepository.save(ride);

        System.out.println("✅ Course #" + rideId + " terminée (paiement espèces confirmé)");

        response.put("success", true);
        response.put("message", "Paiement espèces confirmé, course terminée");
        response.put("rideId", rideId);
        response.put("amount", ride.getEstimatedPrice());

        try {
            notificationService.sendNotificationToClient(
                    ride.getUserId(),
                    "✅ Course terminée",
                    "Le chauffeur a confirmé votre paiement espèces de " + ride.getEstimatedPrice() + " FCFA. Merci !"
            );
        } catch (Exception e) {
            System.err.println("Erreur envoi notification: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}