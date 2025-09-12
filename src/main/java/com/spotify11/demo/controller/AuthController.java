package com.spotify11.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spotify11.demo.services.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final PasswordResetService service;
  public AuthController(PasswordResetService service) { this.service = service; }

  @PostMapping("/forgot")
  public ResponseEntity<Map<String,String>> forgot(@RequestBody Map<String,String> body) {
    service.requestReset(body.getOrDefault("email", ""));
    return ResponseEntity.ok(Map.of("message","If that email exists, a reset link has been sent."));
  }

  public record ResetDto(String token, String password) {}
  @PostMapping("/reset")
  public ResponseEntity<Map<String,String>> reset(@RequestBody ResetDto dto) {
    service.performReset(dto.token(), dto.password());
    return ResponseEntity.ok(Map.of("message","Password updated successfully"));
  }
}