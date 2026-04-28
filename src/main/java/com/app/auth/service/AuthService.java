package com.app.auth.service;

import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.LoginRequest;
import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.security.exception.UnauthorizedException;
import com.app.security.token.JwtProvider;
import com.app.security.token.RedisTokenBlacklistManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    log.info("Login attempt for user: {}", request.email());
    User user = userRepository.findByEmail(request.email()).orElseThrow(UnauthorizedException::new);

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new UnauthorizedException();
    }

    Map<String, Object> claims =
        Map.of(
            "id", user.getId().toString(),
            "email", user.getEmail(),
            "role", user.getRole().toString());

    String token = jwtProvider.generateToken(user.getEmail(), claims);

    return new AuthResponse(token, user.getEmail(), user.getRole().toString());
  }

  public void logout(Jwt jwt) {
    log.info("Logout requested for token: {}", jwt.getId());
    var expiration = jwt.getExpiresAt();
    var jti = jwt.getId();

    if (jti != null && expiration != null) {
      var ttl = Duration.between(Instant.now(), expiration);
      if (!ttl.isNegative()) {
        blacklistManager.blacklist(jti, ttl);
      }
    }
  }
}
