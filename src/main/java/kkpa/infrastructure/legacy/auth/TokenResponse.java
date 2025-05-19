package kkpa.infrastructure.legacy.auth;

import java.time.Instant;

public record TokenResponse(
    String accessToken,
    String tokenType,
    String refreshToken,
    int expiresIn,
    String scope,
    Instant issuedAt) {
  /** Default constructor required for deserialization */
  public TokenResponse {
    // Set issuedAt to current time if not provided
    if (issuedAt == null) {
      issuedAt = Instant.now();
    }
  }

  /**
   * Checks if the token is expired
   *
   * @return true if the token is expired
   */
  public boolean isExpired() {
    return issuedAt.plusSeconds(expiresIn).isBefore(Instant.now());
  }
}
