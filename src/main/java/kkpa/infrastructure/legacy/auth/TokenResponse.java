package kkpa.infrastructure.legacy.auth;

import java.time.Instant;

public record TokenResponse(
    String accessToken, String refreshToken, int expiresIn, Instant issuedAt) {
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

  public static class Builder {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private Instant issuedAt;

    public Builder accessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public Builder refreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public Builder expiresIn(int expiresIn) {
      this.expiresIn = expiresIn;
      return this;
    }

    public Builder issuedAt(Instant issuedAt) {
      this.issuedAt = issuedAt;
      return this;
    }

    public TokenResponse build() {
      return new TokenResponse(accessToken, refreshToken, expiresIn, issuedAt);
    }
  }
}
