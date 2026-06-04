package com.abdil.taxi.controller;

import com.abdil.taxi.model.*;
import com.abdil.taxi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private DriverRepository driverRepository;

    // Ajouter une note et un avis après une course terminée
    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request) {
        System.out.println("=== AJOUT D'UN AVIS ===");
        System.out.println("Ride ID: " + request.getRideId());
        System.out.println("Client ID: " + request.getClientId());
        System.out.println("Note: " + request.getRating());
        System.out.println("Commentaire: " + request.getComment());

        // Vérifier si la course existe
        Ride ride = rideRepository.findById(request.getRideId()).orElse(null);
        if (ride == null) {
            return ResponseEntity.badRequest().body("Course non trouvée");
        }

        // Vérifier que la course est terminée (COMPLETED)
        if (!"COMPLETED".equals(ride.getStatus())) {
            return ResponseEntity.badRequest().body("Vous ne pouvez noter qu'une course terminée");
        }

        // Vérifier que le client est bien celui de la course
        if (!ride.getUserId().equals(request.getClientId())) {
            return ResponseEntity.badRequest().body("Cette course ne vous appartient pas");
        }

        // Vérifier qu'un avis n'existe pas déjà pour cette course
        if (reviewRepository.existsByRideId(request.getRideId())) {
            return ResponseEntity.badRequest().body("Vous avez déjà noté cette course");
        }

        // Vérifier que la note est entre 1 et 5
        if (request.getRating() < 1 || request.getRating() > 5) {
            return ResponseEntity.badRequest().body("La note doit être entre 1 et 5");
        }

        // Créer l'avis
        Review review = new Review();
        review.setRideId(request.getRideId());
        review.setClientId(request.getClientId());
        review.setDriverId(ride.getDriverId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        System.out.println("✅ Avis ajouté avec succès pour la course " + request.getRideId());

        // Mettre à jour la note moyenne du chauffeur
        updateDriverRating(ride.getDriverId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Merci pour votre avis !");
        response.put("review", savedReview);

        return ResponseEntity.ok(response);
    }

    // Mettre à jour la note moyenne du chauffeur
    private void updateDriverRating(Long driverId) {
        Double avgRating = reviewRepository.getAverageRatingByDriverId(driverId);
        Long ratingCount = reviewRepository.getRatingCountByDriverId(driverId);

        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver != null) {
            driver.setRating(avgRating != null ? avgRating : 0.0);
            driver.setRatingCount(ratingCount != null ? ratingCount : 0L);
            driverRepository.save(driver);
            System.out.println("⭐ Chauffeur " + driverId + " - Nouvelle note: " +
                    (avgRating != null ? String.format("%.1f", avgRating) : "0") +
                    " (" + ratingCount + " avis)");
        }
    }

    // Récupérer tous les avis d'un chauffeur
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Review>> getDriverReviews(@PathVariable Long driverId) {
        List<Review> reviews = reviewRepository.findByDriverId(driverId);
        return ResponseEntity.ok(reviews);
    }

    // Récupérer la note moyenne d'un chauffeur
    @GetMapping("/driver/{driverId}/rating")
    public ResponseEntity<Map<String, Object>> getDriverRating(@PathVariable Long driverId) {
        Double avgRating = reviewRepository.getAverageRatingByDriverId(driverId);
        Long ratingCount = reviewRepository.getRatingCountByDriverId(driverId);

        Map<String, Object> response = new HashMap<>();
        response.put("driverId", driverId);
        response.put("averageRating", avgRating != null ? avgRating : 0.0);
        response.put("ratingCount", ratingCount != null ? ratingCount : 0L);

        return ResponseEntity.ok(response);
    }

    // Vérifier si une course a déjà été notée
    @GetMapping("/check/{rideId}")
    public ResponseEntity<Map<String, Boolean>> checkReviewExists(@PathVariable Long rideId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", reviewRepository.existsByRideId(rideId));
        return ResponseEntity.ok(response);
    }

    // Récupérer tous les avis (pour l'admin)
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Review review : reviews) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", review.getId());
            item.put("rideId", review.getRideId());
            item.put("clientId", review.getClientId());
            item.put("driverId", review.getDriverId());

            // Récupérer le nom du chauffeur
            Driver driver = driverRepository.findById(review.getDriverId()).orElse(null);
            item.put("driverName", driver != null ? driver.getFullName() : "Inconnu");

            item.put("rating", review.getRating());
            item.put("comment", review.getComment());
            item.put("createdAt", review.getCreatedAt());
            result.add(item);
        }

        // Trier par date décroissante
        result.sort((a, b) -> ((LocalDateTime)b.get("createdAt")).compareTo((LocalDateTime)a.get("createdAt")));

        return ResponseEntity.ok(result);
    }

    // Récupérer l'avis d'une course spécifique
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<Review> getReviewByRideId(@PathVariable Long rideId) {
        return reviewRepository.findByRideId(rideId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}