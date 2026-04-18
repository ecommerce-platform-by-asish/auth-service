package com.app.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {
  @Test
  public void generateHash() {
    System.out.println("HASH_FOR_PASSWORD=" + new BCryptPasswordEncoder().encode("password"));
  }
}
