package kkpa.infrastructure.legacy.account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kkpa.infrastructure.legacy.api.LegacyApiClientFactory;
import kkpa.infrastructure.legacy.api.LegacyApiException;
import kkpa.infrastructure.legacy.auth.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for accessing account data from the legacy system */
@ApplicationScoped
public class AccountLegacyService {

  private static final Logger log = LoggerFactory.getLogger(AccountLegacyService.class);
  private final LegacyApiClientFactory clientFactory;

  @Inject
  public AccountLegacyService(LegacyApiClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  /**
   * Fetches account information from the legacy system
   *
   * @param token OAuth token for authentication
   * @return Account information
   */
  public AccountDto getAccountInfo(TokenResponse token) {
    try {
      log.debug("Fetching account information");

      AccountApiClient client = clientFactory.createClient(AccountApiClient.class, token);
      AccountDto account = client.getAccountInfo();

      log.debug("Successfully fetched account for: {}", account.email());
      return account;
    } catch (Exception e) {
      log.error("Error fetching account information", e);
      throw new LegacyApiException("Failed to fetch account details", e);
    }
  }
}
