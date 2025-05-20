package kkpa.infrastructure.legacy.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kkpa.infrastructure.legacy.auth.TokenResponse;
import kkpa.infrastructure.legacy.config.LegacyApiConfig;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LegacyApiClientFactory {

  private static final Logger log = LoggerFactory.getLogger(LegacyApiClientFactory.class);
  private final LegacyApiConfig legacyApiConfig;

  @Inject
  public LegacyApiClientFactory(LegacyApiConfig legacyApiConfig) {
    this.legacyApiConfig = legacyApiConfig;
  }

  /**
   * Creates a client for the given API interface with authentication
   *
   * @param apiInterface The REST client interface class
   * @param token The OAuth token to use for authentication
   * @return Configured REST client
   */
  public <T> T createClient(Class<T> apiInterface, TokenResponse token) {
    log.debug("Creating client for {} with token", apiInterface.getSimpleName());
    URI baseUri = UriBuilder.fromUri(legacyApiConfig.serverUrl()).build();

    return RestClientBuilder.newBuilder()
        .baseUri(baseUri)
        .connectTimeout(legacyApiConfig.timeoutSeconds(), TimeUnit.SECONDS)
        .readTimeout(legacyApiConfig.timeoutSeconds(), TimeUnit.SECONDS)
        .register(new LegacyAuthenticationRequestFilter(token))
        .build(apiInterface);
  }

  /** Gets the common headers that should be included in all requests */
  public Map<String, String> getCommonHeaders(TokenResponse token) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.ACCEPT, legacyApiConfig.acceptHeader());
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken());
    return headers;
  }
}
