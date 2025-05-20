package kkpa.infrastructure.legacy.transaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import kkpa.infrastructure.legacy.api.LegacyApiClientFactory;
import kkpa.infrastructure.legacy.api.LegacyApiException;
import kkpa.infrastructure.legacy.auth.RequestScopedTokenHolder;
import kkpa.infrastructure.legacy.auth.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for accessing transaction data from the legacy system */
@ApplicationScoped
public class TransactionLegacyService {

  private static final Logger log = LoggerFactory.getLogger(TransactionLegacyService.class);
  private final LegacyApiClientFactory clientFactory;
  private final RequestScopedTokenHolder tokenHolder;

  @Inject
  public TransactionLegacyService(
      LegacyApiClientFactory clientFactory, RequestScopedTokenHolder tokenHolder) {
    this.clientFactory = clientFactory;
    this.tokenHolder = tokenHolder;
  }

  /**
   * Fetches transactions for a building from the legacy system
   *
   * @param buildingId Building ID to fetch transactions for
   * @return List of transactions or empty list if none found
   */
  public List<TransactionDto> getTransactionsByBuildingId(Long buildingId) {
    try {
      log.debug("Fetching transactions for building ID: {}", buildingId);

      String token = tokenHolder.getAccessToken();
      if (token == null) {
        log.warn("No access token available for transaction request");
        return Collections.emptyList();
      }

      TransactionApiClient client =
          clientFactory.createClient(
              TransactionApiClient.class, new TokenResponse.Builder().accessToken(token).build());
      List<TransactionDto> transactions = client.getTransactionsByBuildingId(buildingId);

      log.debug(
          "Successfully fetched {} transactions for building {}", transactions.size(), buildingId);
      return transactions;
    } catch (Exception e) {
      log.error("Error fetching transactions for building {}", buildingId, e);
      throw new LegacyApiException("Failed to fetch transactions", e);
    }
  }
}
