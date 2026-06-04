package com.policene.eventhub.repository;

import com.policene.eventhub.entity.RefreshToken;
import com.policene.eventhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> getByToken (String token);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeToken (@Param(value = "token") String token);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllByUser(@Param(value = "user") User user);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.used = true WHERE rt.token = :token")
    void consumeToken (@Param(value = "token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true OR rt.used = true")
    void deleteExpiredAndConsumed(@Param("now") LocalDateTime now);

}
