package com.reparasuite.api.repo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findBySubjectIdAndSubjectTypeAndRevokedAtIsNull(UUID subjectId, String subjectType);

  long deleteByExpiresAtBefore(OffsetDateTime cutoff);
}