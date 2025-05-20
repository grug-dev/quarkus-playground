package kkpa.infrastructure.legacy.property;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kkpa.infrastructure.legacy.api.LegacyApiClientFactory;
import kkpa.infrastructure.legacy.api.LegacyApiException;
import kkpa.infrastructure.legacy.auth.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for accessing property data from the legacy system */
@ApplicationScoped
public class PropertyLegacyService {

  private static final Logger log = LoggerFactory.getLogger(PropertyLegacyService.class);
  private final LegacyApiClientFactory clientFactory;

  @Inject
  public PropertyLegacyService(LegacyApiClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  /**
   * Fetches property details from the legacy system
   *
   * @param propertyId Property ID to fetch
   * @param token OAuth token for authentication
   * @return Property information or null if not found
   */
  public PropertyDto getPropertyById(Long propertyId, TokenResponse token) {
    try {
      log.debug("Fetching property with ID: {}", propertyId);

      PropertyApiClient client = clientFactory.createClient(PropertyApiClient.class, token);
      PropertyDto property = client.getPropertyById(propertyId);

      log.debug("Successfully fetched property: {}", property.id());
      return property;
    } catch (Exception e) {
      log.error("Error fetching property with ID: {}", propertyId, e);
      throw new LegacyApiException("Failed to fetch property details", e);
    }
  }
}
