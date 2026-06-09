package com.abdil.taxi.service;

import com.abdil.taxi.model.License;
import com.abdil.taxi.model.User;
import com.abdil.taxi.repository.LicenseRepository;
import com.abdil.taxi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String LICENSE_PREFIX = "ABDIL";

    /**
     * Génère une clé de licence unique
     */
    private String generateUniqueLicenseKey() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String key;
        do {
            StringBuilder sb = new StringBuilder(LICENSE_PREFIX);
            sb.append("-");
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    sb.append(characters.charAt(random.nextInt(characters.length())));
                }
                if (i < 3) sb.append("-");
            }
            key = sb.toString();
        } while (licenseRepository.findByLicenseKey(key).isPresent());
        return key;
    }

    /**
     * Calcule la date de fin en fonction de la durée
     */
    private LocalDateTime calculateEndDate(Integer durationDays) {
        if (durationDays == -1) return null; // Perpétuelle
        return LocalDateTime.now().plusDays(durationDays);
    }

    /**
     * Récupère les infos utilisateur si userId est fourni
     */
    private void enrichUserInfo(License license) {
        if (license.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(license.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                license.setUserName(user.getFullName());
                license.setUserEmail(user.getEmail());
                // Déterminer le type d'utilisateur (CLIENT ou DRIVER)
                if (license.getUserType() == null) {
                    if (user.getRole() != null) {
                        if (user.getRole().contains("DRIVER") || user.getRole().equalsIgnoreCase("DRIVER")) {
                            license.setUserType("DRIVER");
                        } else {
                            license.setUserType("CLIENT");
                        }
                    }
                }
            }
        }
    }

    /**
     * Génère une nouvelle licence
     */
    @Transactional
    public License generateLicense(String licenseType, Integer durationDays, Integer price,
                                   String appType, Long userId, String userType, String createdBy) {
        String licenseKey = generateUniqueLicenseKey();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(durationDays);

        License license = new License(
                licenseKey, licenseType, durationDays, price, appType,
                userId, userType, null, null,
                "ACTIVE", startDate, endDate,
                createdBy != null ? createdBy : "ADMIN"
        );

        enrichUserInfo(license);

        return licenseRepository.save(license);
    }

    /**
     * Récupère toutes les licences
     */
    public List<License> getAllLicenses() {
        List<License> licenses = licenseRepository.findAll();
        licenses.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return licenses;
    }

    /**
     * Vérifie la validité d'une licence
     */
    public Map<String, Object> verifyLicense(String licenseKey, String appType, Long userId) {
        Map<String, Object> result = new HashMap<>();

        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);

        if (licenseOpt.isEmpty()) {
            result.put("valid", false);
            result.put("message", "❌ Clé de licence invalide");
            return result;
        }

        License license = licenseOpt.get();

        // Vérifier le type d'application
        if (!"BOTH".equals(license.getAppType()) && !license.getAppType().equals(appType)) {
            result.put("valid", false);
            result.put("message", "❌ Cette licence n'est pas valide pour cette application");
            return result;
        }

        // Vérifier le statut
        if ("REVOKED".equals(license.getStatus())) {
            result.put("valid", false);
            result.put("message", "❌ Licence révoquée");
            return result;
        }

        // Vérifier l'expiration (sauf pour perpétuelle)
        if (license.getDurationDays() != -1 && license.getEndDate() != null &&
                license.getEndDate().isBefore(LocalDateTime.now())) {
            license.setStatus("EXPIRED");
            licenseRepository.save(license);
            result.put("valid", false);
            result.put("message", "❌ Licence expirée le " + license.getEndDate().toLocalDate());
            return result;
        }

        // Si userId est fourni et licence non attribuée, l'attribuer
        if (userId != null && license.getUserId() == null) {
            license.setUserId(userId);
            license.setUserType(appType);
            enrichUserInfo(license);
            licenseRepository.save(license);
        }

        // Si userId est fourni mais différent, vérifier que c'est le même utilisateur
        if (userId != null && license.getUserId() != null && !license.getUserId().equals(userId)) {
            result.put("valid", false);
            result.put("message", "❌ Cette licence est déjà utilisée par un autre compte");
            return result;
        }

        result.put("valid", true);
        result.put("message", "✅ Licence valide");
        result.put("licenseType", license.getLicenseType());
        result.put("appType", license.getAppType());
        result.put("durationDays", license.getDurationDays());
        result.put("price", license.getPrice());
        result.put("endDate", license.getEndDate());
        result.put("isPerpetual", license.getDurationDays() == -1);

        return result;
    }

    /**
     * Révoque une licence
     */
    @Transactional
    public License revokeLicense(Long licenseId) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("Licence non trouvée"));
        license.setStatus("REVOKED");
        license.setUpdatedAt(LocalDateTime.now());
        return licenseRepository.save(license);
    }

    /**
     * Active une licence (pour réactivation)
     */
    @Transactional
    public License activateLicense(Long licenseId) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("Licence non trouvée"));

        if ("EXPIRED".equals(license.getStatus())) {
            // Réactiver avec nouvelle date d'expiration
            LocalDateTime newEndDate = calculateEndDate(license.getDurationDays());
            license.setEndDate(newEndDate);
        }

        license.setStatus("ACTIVE");
        license.setUpdatedAt(LocalDateTime.now());
        return licenseRepository.save(license);
    }

    /**
     * Met à jour les licences expirées
     */
    @Transactional
    public int updateExpiredLicenses() {
        List<License> expiredLicenses = licenseRepository.findExpiredActiveLicenses(LocalDateTime.now());
        for (License license : expiredLicenses) {
            license.setStatus("EXPIRED");
            license.setUpdatedAt(LocalDateTime.now());
        }
        licenseRepository.saveAll(expiredLicenses);
        return expiredLicenses.size();
    }

    /**
     * Récupère les statistiques des licences
     */
    public Map<String, Object> getLicenseStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", licenseRepository.count());
        stats.put("active", licenseRepository.countActiveLicenses());

        Long totalRevenue = licenseRepository.sumTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0L);

        // Statistiques par type
        List<Object[]> byType = licenseRepository.countByLicenseType();
        Map<String, Long> byTypeMap = new HashMap<>();
        for (Object[] row : byType) {
            byTypeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("byType", byTypeMap);

        return stats;
    }

    /**
     * Récupère la licence d'un utilisateur
     */
    public Optional<License> getUserActiveLicense(Long userId, String appType) {
        return licenseRepository.findByUserIdAndAppTypeAndStatus(userId, appType, "ACTIVE");
    }

    /**
     * Récupère les licences d'un utilisateur
     */
    public List<License> getUserLicenses(Long userId, String appType) {
        return licenseRepository.findByUserIdAndAppType(userId, appType);
    }
}