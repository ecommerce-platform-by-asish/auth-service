package com.ecommerce.auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class JwksController {

  private final KeyPair keyPair;

  @Value("${jwt.key-id:common-auth-key-1}")
  private String keyId;

  @GetMapping("/jwks.json")
  public Map<String, Object> getJwks() {
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAKey jwk = new RSAKey.Builder(publicKey).keyID(keyId).build();
    return new JWKSet(jwk).toJSONObject();
  }
}
