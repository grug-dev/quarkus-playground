package kkpa.infrastructure.legacy.auth;

import jakarta.enterprise.context.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request-scoped service to hold the current user's access token A new instance is created for each
 * HTTP request
 */
@RequestScoped
public class RequestScopedTokenHolder {

  private static final Logger log = LoggerFactory.getLogger(RequestScopedTokenHolder.class);
  private String accessToken;

  public RequestScopedTokenHolder() {
    log.info("RequestScopedTokenHolder created");
  }

  /** Sets the access token for the current request */
  public void setAccessToken(String accessToken) {
    if (accessToken == null || accessToken.isEmpty()) {
      log.warn("Attempt to set null or empty access token");
      return;
    }

    String maskedToken = maskToken(accessToken);
    log.debug("Setting access token for current request: {}", maskedToken);
    this.accessToken = accessToken;
  }

  /** Gets the access token for the current request */
  public String getAccessToken() {
    if (accessToken == null || accessToken.isEmpty()) {
      log.warn("Access token is null or empty for current request");
    }
    return accessToken;
  }

  /** Masks a token for logging purposes */
  private String maskToken(String token) {
    if (token == null || token.length() < 8) {
      return "***masked***";
    }
    // Show only first 4 and last 4 characters
    return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
  }
}
