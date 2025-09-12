package com.spotify11.demo.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.spotify11.demo.entity.PasswordResetToken;
import com.spotify11.demo.repo.PasswordResetTokenRepo;
import com.spotify11.demo.repo.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PasswordResetService {
  private final UserRepository users;
  private final PasswordResetTokenRepo tokens;

  @Autowired
  private JavaMailSender mailSender;

  private final PasswordEncoder encoder;

  @Value("${app.frontend.reset-url:${APP_FRONTEND_RESET_URL:http://localhost:5173/reset}}")
  String resetUrl;
  public PasswordResetService(UserRepository users, PasswordResetTokenRepo tokens,
                              JavaMailSender mailSender, PasswordEncoder encoder) {
    this.users = users; this.tokens = tokens; this.mailSender = mailSender; this.encoder = encoder;
  }

  @Transactional
  public void requestReset(String email) {
    users.findByEmail(email).ifPresent(user -> {
      // (optional) invalidate old tokens for this user here
      var prt = new PasswordResetToken();
      prt.setToken(UUID.randomUUID().toString());
      prt.setUser(user);
      prt.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
      tokens.save(prt);

      sendResetEmail(user.getEmail(), prt.getToken());
    });
    // Always return OK; don't reveal whether email exists
  }

  @Transactional
  public void performReset(String tokenValue, String newPassword) {
    var prt = tokens.findByToken(tokenValue)
        .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));
    if (prt.isUsed() || prt.getExpiresAt().isBefore(Instant.now())) {
      throw new RuntimeException("Invalid or expired reset link");
    }

    var user = prt.getUser();
    user.setPassword(encoder.encode(newPassword));
    prt.setUsed(true);
    // (optional) user.setPasswordUpdatedAt(Instant.now());
  }

  private void sendResetEmail(String to, String token) {
    String link = resetUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    var msg = new org.springframework.mail.SimpleMailMessage();
    msg.setTo(to);
    msg.setFrom("no-reply@yourapp.com");   // set a verified from address
    msg.setSubject("Reset your password");
    msg.setText("Click the link to reset your password (valid 15 minutes):\n" + link);
    mailSender.send(msg);
  }
}
