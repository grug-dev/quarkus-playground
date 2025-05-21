package kkpa.application.assistants;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import kkpa.infrastructure.legacy.transaction.TransactionLegacyService;
import kkpa.tools.PropertyTools;

@ApplicationScoped
public class ToolsFactory {

  private final TransactionLegacyService transactionService;

  @Inject
  public ToolsFactory(TransactionLegacyService transactionService) {
    this.transactionService = transactionService;
  }

  @Produces
  @ApplicationScoped
  public PropertyTools createPropertyTools() {
    return new PropertyTools(transactionService);
  }
}
