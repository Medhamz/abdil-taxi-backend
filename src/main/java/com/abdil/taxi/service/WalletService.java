package com.abdil.taxi.service;

import com.abdil.taxi.model.RechargeCoupon;
import com.abdil.taxi.model.Wallet;
import com.abdil.taxi.model.WalletTransaction;
import com.abdil.taxi.repository.RechargeCouponRepository;
import com.abdil.taxi.repository.WalletRepository;
import com.abdil.taxi.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Autowired
    private RechargeCouponRepository couponRepository;

    @Autowired
    private PaymentService paymentService;

    private static final String COUPON_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
    private static final int COUPON_LENGTH = 8;

    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(userId);
                    newWallet.setBalance(0.0);
                    return walletRepository.save(newWallet);
                });
    }

    public Double getBalance(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getBalance)
                .orElse(0.0);
    }

    @Transactional
    public WalletTransaction rechargeWallet(Long userId, Double amount, String paymentMethod, String phoneNumber) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount);
        transaction.setType("CREDIT");
        transaction.setStatus("COMPLETED");
        transaction.setReference("RECH_" + UUID.randomUUID().toString());
        transaction.setDescription("Recharge via " + paymentMethod);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public boolean debitWallet(Long userId, Double amount, String rideId) {
        int updated = walletRepository.debitBalance(userId, amount);

        if (updated > 0) {
            Wallet wallet = getOrCreateWallet(userId);

            WalletTransaction transaction = new WalletTransaction();
            transaction.setWalletId(wallet.getId());
            transaction.setAmount(amount);
            transaction.setType("DEBIT");
            transaction.setStatus("COMPLETED");
            transaction.setReference("PAY_" + rideId + "_" + System.currentTimeMillis());
            transaction.setDescription("Paiement course #" + rideId);
            transactionRepository.save(transaction);

            return true;
        }
        return false;
    }

    public List<WalletTransaction> getTransactionHistory(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    // ==================== COUPON METHODS ====================

    private String generateUniqueCode() {
        Random random = new Random();
        StringBuilder sb;
        boolean exists;

        do {
            sb = new StringBuilder();
            for (int i = 0; i < COUPON_LENGTH; i++) {
                sb.append(COUPON_CHARS.charAt(random.nextInt(COUPON_CHARS.length())));
            }
            exists = couponRepository.findByCodeAndStatus(sb.toString(), "ACTIVE").isPresent();
        } while (exists);

        return sb.toString();
    }

    public List<RechargeCoupon> generateCoupons(int count, double amount, String createdBy, int validityDays) {
        List<RechargeCoupon> coupons = new ArrayList<>();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validityDays);

        for (int i = 0; i < count; i++) {
            RechargeCoupon coupon = new RechargeCoupon();
            coupon.setCode(generateUniqueCode());
            coupon.setAmount(amount);
            coupon.setStatus("ACTIVE");
            coupon.setCreatedBy(createdBy);
            coupon.setExpiresAt(expiresAt);
            coupons.add(coupon);
        }

        return couponRepository.saveAll(coupons);
    }

    @Transactional
    public Map<String, Object> useCoupon(String code, Long userId) {
        Map<String, Object> result = new HashMap<>();

        Optional<RechargeCoupon> optionalCoupon = couponRepository.findByCodeAndStatus(code, "ACTIVE");

        if (optionalCoupon.isEmpty()) {
            result.put("success", false);
            result.put("message", "Code invalide ou déjà utilisé");
            return result;
        }

        RechargeCoupon coupon = optionalCoupon.get();

        // Vérifier si le coupon n'est pas expiré
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            result.put("success", false);
            result.put("message", "Ce coupon a expiré");
            return result;
        }

        // Marquer le coupon comme utilisé
        int updated = couponRepository.useCoupon(code, userId);

        if (updated > 0) {
            // Créditer le wallet
            Wallet wallet = getOrCreateWallet(userId);
            wallet.setBalance(wallet.getBalance() + coupon.getAmount());
            walletRepository.save(wallet);

            // Enregistrer la transaction
            WalletTransaction transaction = new WalletTransaction();
            transaction.setWalletId(wallet.getId());
            transaction.setAmount(coupon.getAmount());
            transaction.setType("CREDIT");
            transaction.setStatus("COMPLETED");
            transaction.setReference("COUPON_" + code);
            transaction.setDescription("Recharge par coupon: " + code);
            transactionRepository.save(transaction);

            result.put("success", true);
            result.put("message", "Recharge de " + coupon.getAmount() + " FCFA effectuée !");
            result.put("newBalance", wallet.getBalance());
        } else {
            result.put("success", false);
            result.put("message", "Erreur lors du rechargement");
        }

        return result;
    }

    public List<RechargeCoupon> getAllCoupons() {
        return couponRepository.findAll();
    }
}