package kkpa.application.web;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import kkpa.infrastructure.legacy.auth.RequestScopedTokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that extracts the access token from incoming requests and stores it in the
 * RequestScopedTokenHolder
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AccessTokenFilter implements ContainerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AccessTokenFilter.class);
  private static final String BEARER_PREFIX = "Bearer ";

  @Inject RequestScopedTokenHolder tokenHolder;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    try {
      String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
      log.info("Extracting access token from request");
      if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        tokenHolder.setAccessToken(token);
      } else {
        log.debug("No Bearer token found in request");
      }
    } catch (Exception e) {
      log.error("Error extracting access token", e);
    }
  }
}
