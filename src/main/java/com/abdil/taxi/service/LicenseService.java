package com.abdil.taxi.service;

import com.abdil.taxi.model.License;
import com.abdil.taxi.model.Driver;
import com.abdil.taxi.repository.LicenseRepository;
import com.abdil.taxi.repository.DriverRepository;
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
    private DriverRepository driverRepository;  // ✅ CHANGÉ: DriverRepository au lieu de UserRepository

    private static final String LICENSE_PREFIX = "ABDIL";

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

    private LocalDateTime calculateEndDate(Integer durationDays) {
        if (durationDays == -1) return null;
        return LocalDateTime.now().plusDays(durationDays);
    }

    // ✅ CORRIGÉ: Utilise DriverRepository
    private void enrichDriverInfo(License license) {
        if (license.getUserId() != null) {
            Optional<Driver> driverOpt = driverRepository.findById(license.getUserId());
            if (driverOpt.isPresent()) {
                Driver driver = driverOpt.get();
                license.setUserName(driver.getFullName());
                license.setUserEmail(driver.getEmail());
                System.out.println("📝 enrichDriverInfo: " + driver.getFullName() + " pour licence " + license.getLicenseKey());
                if (license.getUserType() == null) {
                    license.setUserType("DRIVER");
                }
            }
        }
    }

    @Transactional
    public License generateLicense(String licenseType, Integer durationDays, Integer price,
                                   String appType, Long userId, String userType, String createdBy) {

        // ✅ Forcer l'application à DRIVER
        if (appType == null || (!"DRIVER".equals(appType) && !"BOTH".equals(appType))) {
            appType = "DRIVER";
        }

        // ✅ Vérifier si le chauffeur a déjà une licence active
        if (userId != null) {
            Optional<License> existingLicense = licenseRepository.findByUserIdAndAppTypeAndStatus(userId, appType, "ACTIVE");
            if (existingLicense.isPresent()) {
                throw new RuntimeException("Ce chauffeur a déjà une licence active");
            }
        }

        String licenseKey = generateUniqueLicenseKey();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(durationDays);

        License license = new License(
                licenseKey, licenseType, durationDays, price, appType,
                userId, "DRIVER", null, null,
                "ACTIVE", startDate, endDate,
                createdBy != null ? createdBy : "ADMIN"
        );

        enrichDriverInfo(license);

        return licenseRepository.save(license);
    }

    public List<License> getAllLicenses() {
        List<License> licenses = licenseRepository.findAll();

        // ✅ Mettre à jour les noms manquants avant d'afficher
        for (License license : licenses) {
            if (license.getUserId() != null && (license.getUserName() == null || license.getUserName().isEmpty())) {
                enrichDriverInfo(license);
                if (license.getUserName() != null) {
                    licenseRepository.save(license);
                }
            }
        }

        licenses.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return licenses;
    }

    @Transactional
    public Map<String, Object> verifyLicense(String licenseKey, String appType, Long userId) {
        Map<String, Object> result = new HashMap<>();

        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);

        if (licenseOpt.isEmpty()) {
            result.put("valid", false);
            result.put("message", "❌ Clé de licence invalide");
            return result;
        }

        License license = licenseOpt.get();

        // ✅ Vérifier si la licence est déjà utilisée par un autre chauffeur
        if (license.getUserId() != null && userId != null && !license.getUserId().equals(userId)) {
            result.put("valid", false);
            result.put("message", "❌ Cette licence est déjà utilisée par un autre compte");
            return result;
        }

        // ✅ Vérifier le type d'application
        if (!"BOTH".equals(license.getAppType()) && !license.getAppType().equals(appType)) {
            result.put("valid", false);
            result.put("message", "❌ Cette licence n'est pas valide pour cette application");
            return result;
        }

        // ✅ Vérifier le statut
        if ("REVOKED".equals(license.getStatus())) {
            result.put("valid", false);
            result.put("message", "❌ Licence révoquée");
            return result;
        }

        // ✅ Vérifier l'expiration
        if (license.getDurationDays() != -1 && license.getEndDate() != null &&
                license.getEndDate().isBefore(LocalDateTime.now())) {
            license.setStatus("EXPIRED");
            licenseRepository.save(license);
            result.put("valid", false);
            result.put("message", "❌ Licence expirée le " + license.getEndDate().toLocalDate());
            return result;
        }

        // ✅ Si la licence n'est pas encore attribuée, l'attribuer à ce chauffeur
        if (license.getUserId() == null && userId != null) {
            // ✅ Vérifier si le chauffeur n'a pas déjà une autre licence active
            Optional<License> userActiveLicense = licenseRepository.findByUserIdAndAppTypeAndStatus(userId, appType, "ACTIVE");
            if (userActiveLicense.isPresent() && !userActiveLicense.get().getId().equals(license.getId())) {
                result.put("valid", false);
                result.put("message", "❌ Vous avez déjà une licence active. Une seule licence par compte est autorisée.");
                return result;
            }

            license.setUserId(userId);
            license.setUserType("DRIVER");

            // ✅ Récupérer les informations complètes du chauffeur
            Optional<Driver> driverOpt = driverRepository.findById(userId);
            if (driverOpt.isPresent()) {
                Driver driver = driverOpt.get();
                license.setUserName(driver.getFullName());
                license.setUserEmail(driver.getEmail());
                license.setUserType("DRIVER");

                System.out.println("✅ Licence attribuée au chauffeur: " + driver.getFullName() + " (" + driver.getEmail() + ")");
            } else {
                System.out.println("⚠️ Avertissement: Chauffeur non trouvé avec ID: " + userId);
            }

            licenseRepository.save(license);
        } else if (license.getUserId() != null && (license.getUserName() == null || license.getUserName().isEmpty()) && userId != null) {
            // ✅ Cas où la licence est déjà attribuée mais le nom n'a pas été sauvegardé
            Optional<Driver> driverOpt = driverRepository.findById(license.getUserId());
            if (driverOpt.isPresent()) {
                Driver driver = driverOpt.get();
                license.setUserName(driver.getFullName());
                license.setUserEmail(driver.getEmail());
                licenseRepository.save(license);
                System.out.println("✅ Mise à jour du nom pour la licence: " + driver.getFullName());
            }
        }

        result.put("valid", true);
        result.put("message", "✅ Licence valide");
        result.put("licenseType", license.getLicenseType());
        result.put("appType", license.getAppType());
        result.put("durationDays", license.getDurationDays());
        result.put("price", license.getPrice());
        result.put("endDate", license.getEndDate());
        result.put("isPerpetual", license.getDurationDays() == -1);
        result.put("userName", license.getUserName());
        result.put("userEmail", license.getUserEmail());

        return result;
    }

    @Transactional
    public License revokeLicense(Long licenseId) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("Licence non trouvée"));
        license.setStatus("REVOKED");
        license.setUpdatedAt(LocalDateTime.now());
        return licenseRepository.save(license);
    }

    @Transactional
    public License activateLicense(Long licenseId) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("Licence non trouvée"));

        // ✅ Vérifier si le chauffeur n'a pas déjà une autre licence active
        if (license.getUserId() != null) {
            Optional<License> existingLicense = licenseRepository.findByUserIdAndAppTypeAndStatus(
                    license.getUserId(), license.getAppType(), "ACTIVE");
            if (existingLicense.isPresent() && !existingLicense.get().getId().equals(licenseId)) {
                throw new RuntimeException("Ce chauffeur a déjà une licence active");
            }
        }

        if ("EXPIRED".equals(license.getStatus())) {
            LocalDateTime newEndDate = calculateEndDate(license.getDurationDays());
            license.setEndDate(newEndDate);
        }

        license.setStatus("ACTIVE");
        license.setUpdatedAt(LocalDateTime.now());

        // ✅ Mettre à jour les infos du chauffeur si besoin
        enrichDriverInfo(license);

        return licenseRepository.save(license);
    }

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

    public Map<String, Object> getLicenseStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", licenseRepository.count());
        stats.put("active", licenseRepository.countActiveLicenses());

        Long totalRevenue = licenseRepository.sumTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0L);

        List<Object[]> byType = licenseRepository.countByLicenseType();
        Map<String, Long> byTypeMap = new HashMap<>();
        for (Object[] row : byType) {
            byTypeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("byType", byTypeMap);

        return stats;
    }

    public Optional<License> getUserActiveLicense(Long userId, String appType) {
        return licenseRepository.findByUserIdAndAppTypeAndStatus(userId, appType, "ACTIVE");
    }

    public List<License> getUserLicenses(Long userId, String appType) {
        return licenseRepository.findByUserIdAndAppType(userId, appType);
    }
}