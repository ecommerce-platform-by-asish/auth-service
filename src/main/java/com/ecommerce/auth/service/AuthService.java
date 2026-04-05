package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.security.exception.UnauthorizedException;
import com.ecommerce.security.jwt.JwtProvider;
import com.ecommerce.security.jwt.RedisTokenBlacklistManager;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RedisTokenBlacklistManager blacklistManager;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtProvider jwtProvider,
      RedisTokenBlacklistManager blacklistManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtProvider = jwtProvider;
    this.blacklistManager = blacklistManager;
  }

  public AuthResponse login(LoginRequest request) {
    User user =
        userRepository.findByEmail(request.getEmail()).orElseThrow(UnauthorizedException::new);

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException();
    }

    Map<String, Object> claims =
        Map.of("id", user.getId(), "email", user.getEmail(), "role", user.getRole().toString());

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
    Date expiration = claims.getExpiration();

    if (jti != null && expiration != null && blacklistManager != null) {
      long timeToLiveMs = expiration.getTime() - System.currentTimeMillis();
      if (timeToLiveMs > 0) {
        blacklistManager.blacklist(jti, Duration.ofMillis(timeToLiveMs));
      }
    }
  }
}
