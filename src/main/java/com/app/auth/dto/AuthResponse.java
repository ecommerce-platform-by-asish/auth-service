package com.app.auth.dto;

public record AuthResponse(String token, String type, String email, String role) {
  public AuthResponse {
    if (type == null) {
      type = "Bearer";
    }
  }

  // Convenience constructor to match common usage without explicit type
  public AuthResponse(String token, String email, String role) {
    this(token, "Bearer", email, role);
  }
}
