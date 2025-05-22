package kkpa.application.assistants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import kkpa.infrastructure.legacy.transaction.TransactionLegacyResponse;

@JsonInclude(Include.NON_NULL)
public record PropertyData(
    // For single property responses
    Property property,

    // For multiple properties
    List<Property> properties,
    List<TransactionLegacyResponse> transactions,

    // For city count responses
    List<Counts> cities) {}
