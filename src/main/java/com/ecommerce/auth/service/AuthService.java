package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.security.exception.UnauthorizedException;
import com.security.jwt.JwtProvider;
import com.security.jwt.RedisTokenBlacklistManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RedisTokenBlacklistManager blacklistManager;

  public AuthResponse login(LoginRequest request) {
    log.info("Login attempt for user: {}", request.getEmail());
    User user =
        userRepository.findByEmail(request.getEmail()).orElseThrow(UnauthorizedException::new);

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException();
    }

    Map<String, Object> claims =
        Map.of(
            "id", user.getId().toString(),
            "email", user.getEmail(),
            "role", user.getRole().toString());

    String token = jwtProvider.generateToken(user.getEmail(), claims);

    return AuthResponse.builder()
        .token(token)
        .email(user.getEmail())
        .role(user.getRole().toString())
        .build();
  }

  public void logout(String token) {
    var claims = jwtProvider.extractClaims(token);
    String jti = claims.getId();
    Instant expiration = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;

    if (jti != null && expiration != null && blacklistManager != null) {
      Duration timeToLive = Duration.between(Instant.now(), expiration);
      if (!timeToLive.isNegative()) {
        blacklistManager.blacklist(jti, timeToLive);
      }
    }
  }
}
