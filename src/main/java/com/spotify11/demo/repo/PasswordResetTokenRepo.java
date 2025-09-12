package com.spotify11.demo.repo;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.spotify11.demo.entity.PasswordResetToken;

import jakarta.transaction.Transactional;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);
  @Modifying @Transactional
  void deleteByExpiresAtBefore(Instant cutoff);
}
