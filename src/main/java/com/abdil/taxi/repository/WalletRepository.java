package com.abdil.taxi.repository;

import com.abdil.taxi.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.userId = :userId")
    int creditBalance(Long userId, Double amount);

    @Modifying
    @Transactional
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount WHERE w.userId = :userId AND w.balance >= :amount")
    int debitBalance(Long userId, Double amount);
}