package com.spotify11.demo.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter @Setter
public class PasswordResetToken {
  @Id @GeneratedValue private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User user;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean used = false;
}
