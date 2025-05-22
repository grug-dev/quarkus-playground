package kkpa.infrastructure.legacy.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Data transfer object for real estate transactions */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionLegacyResponse(
    Long id, String type, BigDecimal txnArea, BigDecimal txnAmount, LocalDate txnDate) {}
