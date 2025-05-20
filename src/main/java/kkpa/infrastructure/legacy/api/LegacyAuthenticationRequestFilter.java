package kkpa.infrastructure.legacy.api;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import kkpa.infrastructure.legacy.auth.TokenResponse;

/** Filter to add authentication headers to outgoing requests */
public class LegacyAuthenticationRequestFilter implements ClientRequestFilter {

  private final TokenResponse token;
  private final String accessToken;

  public LegacyAuthenticationRequestFilter(TokenResponse token) {
    this.token = token;
    this.accessToken = null;
  }

  public LegacyAuthenticationRequestFilter(String accessToken) {
    this.accessToken = accessToken;
    this.token = null;
  }

  @Override
  public void filter(ClientRequestContext requestContext) {
    // Add Bearer token to Authorization header
    if (token != null && token.accessToken() != null) {
      requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken());
    }
    if (accessToken != null) {
      requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }
  }
}
