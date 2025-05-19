package kkpa.infrastructure.legacy.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for handling OAuth token operations */
@ApplicationScoped
public class OAuthService {

  private final OAuthConfig config;
  private final OAuthClient oauthClient;

  private Logger Log = LoggerFactory.getLogger(OAuthService.class);

  // Simple cache for tokens (could be replaced with a proper caching solution)
  private final Map<String, TokenResponse> tokenCache = new ConcurrentHashMap<>();

  @Inject
  public OAuthService(OAuthConfig config) {

    this.config = config;

    // Build the REST client for the OAuth server
    URI serverUri = UriBuilder.fromUri(config.serverUrl()).build();
    this.oauthClient = RestClientBuilder.newBuilder().baseUri(serverUri).build(OAuthClient.class);
    Log.info("OAuth client created for server: " + serverUri);
  }

  /**
   * Gets a token for the given credentials, using cache if available and not expired
   *
   * @param credentials User credentials
   * @return TokenResponse containing the access token and related information
   */
  public TokenResponse getToken(Credentials credentials) {
    // Check cache first
    String cacheKey = credentials.username();
    TokenResponse cachedToken = tokenCache.get(cacheKey);

    if (cachedToken != null && !cachedToken.isExpired()) {
      Log.debug("Using cached token for user: " + credentials.username());
      return cachedToken;
    }

    // Cache miss or expired token, request a new one
    TokenResponse token = requestToken(credentials);
    tokenCache.put(cacheKey, token);
    return token;
  }

  /**
   * Requests a new token from the OAuth server
   *
   * @param credentials User credentials
   * @return TokenResponse containing the access token
   */
  private TokenResponse requestToken(Credentials credentials) {
    try {
      // Prepare authorization header
      String auth = config.clientId() + ":" + config.clientSecret();
      String encodedAuth =
          Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
      String authHeader = "Basic " + encodedAuth;

      // Prepare form data
      MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
      formData.add("username", credentials.username());
      formData.add("password", credentials.password());
      formData.add("grant_type", config.grantType());
      formData.add("scope", config.scope());
      formData.add("client_id", config.clientId());
      formData.add("client_secret", config.clientSecret());

      Log.debug("Requesting token for user: " + credentials.username());

      // Make the token request
      TokenResponseDto dto = oauthClient.getToken(authHeader, formData);

      // Convert and return the token response
      return new TokenResponse(
          dto.access_token(),
          dto.token_type(),
          dto.refresh_token(),
          dto.expires_in(),
          dto.scope(),
          Instant.now());
    } catch (Exception e) {
      Log.error("Failed to get token", e);
      throw new RuntimeException("Authentication failed", e);
    }
  }

  /**
   * Invalidates a cached token for a user
   *
   * @param username Username to invalidate
   */
  public void invalidateToken(String username) {
    tokenCache.remove(username);
  }
}
