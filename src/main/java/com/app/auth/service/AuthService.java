package com.app.auth.service;

import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.LoginRequest;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.security.exception.UnauthorizedException;
import com.app.security.model.Role;
import com.app.security.token.JwtProvider;
import com.app.security.token.RedisTokenBlacklistManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RedisTokenBlacklistManager blacklistManager;

  @Transactional
  public void register(RegisterRequest request) {
    log.info("Registering user: {}", request.email());
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new RuntimeException("Email already exists");
    }

    userRepository.save(
        User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(Role.USER)
            .build());
  }

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

    String token = jwtProvider.generateToken(user.getId().toString(), claims);

    return new AuthResponse(token, user.getEmail(), user.getRole().toString());
  }

  public void logout(Jwt jwt) {
    log.info("Logout requested for token: {}", jwt.getId());
    Optional.ofNullable(jwt.getId())
        .ifPresent(
            jti ->
                Optional.ofNullable(jwt.getExpiresAt())
                    .map(exp -> Duration.between(Instant.now(), exp))
                    .filter(ttl -> !ttl.isNegative())
                    .ifPresent(ttl -> blacklistManager.blacklist(jti, ttl)));
  }
}
