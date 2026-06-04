package com.abdil.taxi.repository;

import com.abdil.taxi.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<UserToken, Long> {
    List<UserToken> findByUserId(Long userId);
    List<UserToken> findByToken(String token);
    List<UserToken> findByUserType(String userType);
    Optional<UserToken> findFirstByToken(String token);
    List<UserToken> findByUserIdAndUserType(Long userId, String userType);
}