package kkpa.tools;

import dev.langchain4j.agent.tool.Tool;
import java.math.BigDecimal;
import java.util.List;
import kkpa.infrastructure.legacy.transaction.TransactionLegacyResponse;
import kkpa.infrastructure.legacy.transaction.TransactionLegacyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tools for retrieving property-related information */
public class PropertyTools {

  private static final Logger log = LoggerFactory.getLogger(PropertyTools.class);
  private final TransactionLegacyService transactionService;

  public PropertyTools(TransactionLegacyService transactionService) {
    this.transactionService = transactionService;
  }

  @Tool(
      "IMPORTANT: You MUST use this tool anytime a user asks about transactions, sales history, or financial activities for a specific building. When you see a building ID in a question about transactions, call this tool with that ID. Example: If asked 'Show me transactions for building 500158', call this tool with buildingId=500158.")
  public String getTransactionsForBuilding(Long buildingId) {
    try {
      log.info("Tool called: getTransactionsForBuilding with ID: {}", buildingId);

      List<TransactionLegacyResponse> transactions =
          transactionService.getTransactionsByBuildingId(buildingId);

      if (transactions.isEmpty()) {
        return "No transactions found for building ID " + buildingId;
      }

      // Format transactions in a way the LLM can easily understand
      StringBuilder sb = new StringBuilder();
      sb.append("Found ")
          .append(transactions.size())
          .append(" transactions for building ID ")
          .append(buildingId)
          .append(":\n\n");

      for (TransactionLegacyResponse tx : transactions) {
        sb.append("- Transaction ID: ")
            .append(tx.id())
            .append(", Type: ")
            .append(tx.type())
            .append(", Amount: ")
            .append(tx.txnAmount())
            .append(", Date: ")
            .append(tx.txnDate());

        sb.append("\n");
      }

      log.info("Transaction Tool Response {}", sb.toString());

      return sb.toString();
    } catch (Exception e) {
      log.error("Error in getTransactionsForBuilding tool", e);
      return "Error retrieving transactions: " + e.getMessage();
    }
  }

  /** Helper method to format currency values */
  /** NOT USE this, it fails in runtije because tries to convert to bigdecimal */
  public String formatCurrency(BigDecimal amount, String currencyCode) {
    if (amount == null) {
      return "N/A";
    }

    if ("BRL".equalsIgnoreCase(currencyCode)) {
      return "USD " + amount.toString();
    } else {
      return amount.toString() + " " + currencyCode;
    }
  }
}
