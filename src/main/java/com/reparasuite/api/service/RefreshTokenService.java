package com.reparasuite.api.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reparasuite.api.exception.UnauthorizedException;
import com.reparasuite.api.model.RefreshToken;
import com.reparasuite.api.repo.RefreshTokenRepo;

@Service
public class RefreshTokenService {

  private final RefreshTokenRepo refreshTokenRepo;
  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${reparasuite.refresh-token.exp-days:14}")
  private long refreshTokenExpDays;

  public RefreshTokenService(RefreshTokenRepo refreshTokenRepo) {
    this.refreshTokenRepo = refreshTokenRepo;
  }

  @Transactional
  public String createToken(
      UUID subjectId,
      String subjectType,
      String ip,
      String userAgent
  ) {
    String rawToken = generateOpaqueToken();
    String hash = sha256(rawToken);

    RefreshToken token = new RefreshToken();
    token.setSubjectId(subjectId);
    token.setSubjectType(subjectType);
    token.setTokenHash(hash);
    token.setExpiresAt(OffsetDateTime.now().plusDays(Math.max(refreshTokenExpDays, 1)));
    token.setIpAddress(trim(ip, 100));
    token.setUserAgent(trim(userAgent, 500));

    refreshTokenRepo.save(token);
    return rawToken;
  }

  @Transactional
  public RefreshToken rotateToken(
      String rawRefreshToken,
      String ip,
      String userAgent
  ) {
    RefreshToken current = getValidToken(rawRefreshToken);

    current.setRevokedAt(OffsetDateTime.now());
    current.setLastUsedAt(OffsetDateTime.now());

    String newRawToken = generateOpaqueToken();
    String newHash = sha256(newRawToken);
    current.setReplacedByTokenHash(newHash);
    refreshTokenRepo.save(current);

    RefreshToken next = new RefreshToken();
    next.setSubjectId(current.getSubjectId());
    next.setSubjectType(current.getSubjectType());
    next.setTokenHash(newHash);
    next.setExpiresAt(OffsetDateTime.now().plusDays(Math.max(refreshTokenExpDays, 1)));
    next.setIpAddress(trim(ip, 100));
    next.setUserAgent(trim(userAgent, 500));

    refreshTokenRepo.save(next);

    RefreshToken wrapped = new RefreshToken();
    wrapped.setSubjectId(next.getSubjectId());
    wrapped.setSubjectType(next.getSubjectType());
    wrapped.setTokenHash(next.getTokenHash());
    wrapped.setExpiresAt(next.getExpiresAt());
    wrapped.setIpAddress(next.getIpAddress());
    wrapped.setUserAgent(next.getUserAgent());
    wrapped.setLastUsedAt(next.getLastUsedAt());
    wrapped.setReplacedByTokenHash(newRawToken);

    return wrapped;
  }

  @Transactional
  public void revokeToken(String rawRefreshToken) {
    String hash = sha256(rawRefreshToken);
    refreshTokenRepo.findByTokenHash(hash).ifPresent(token -> {
      if (!token.isRevoked()) {
        token.setRevokedAt(OffsetDateTime.now());
        token.setLastUsedAt(OffsetDateTime.now());
        refreshTokenRepo.save(token);
      }
    });
  }

  @Transactional
  public void revokeAllBySubject(UUID subjectId, String subjectType) {
    refreshTokenRepo.findBySubjectIdAndSubjectTypeAndRevokedAtIsNull(subjectId, subjectType)
        .forEach(token -> {
          token.setRevokedAt(OffsetDateTime.now());
          token.setLastUsedAt(OffsetDateTime.now());
          refreshTokenRepo.save(token);
        });
  }

  @Transactional(readOnly = true)
  public RefreshToken getValidToken(String rawRefreshToken) {
    String hash = sha256(rawRefreshToken);

    RefreshToken token = refreshTokenRepo.findByTokenHash(hash)
        .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

    if (token.isRevoked()) {
      throw new UnauthorizedException("Refresh token revocado");
    }

    if (token.isExpired()) {
      throw new UnauthorizedException("Refresh token expirado");
    }

    return token;
  }

  private String generateOpaqueToken() {
    byte[] bytes = new byte[48];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception ex) {
      throw new IllegalStateException("No fue posible calcular hash SHA-256", ex);
    }
  }

  private String trim(String value, int max) {
    if (value == null) return null;
    String out = value.trim();
    if (out.isBlank()) return null;
    return out.length() <= max ? out : out.substring(0, max);
  }
}