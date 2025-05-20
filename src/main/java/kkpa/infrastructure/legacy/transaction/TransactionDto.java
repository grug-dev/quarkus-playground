package kkpa.infrastructure.legacy.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Data transfer object for real estate transactions */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionDto(
    Long id,
    Long buildingId,
    String transactionType,
    BigDecimal amount,
    String currency,
    LocalDate transactionDate,
    String status,
    String description) {}
