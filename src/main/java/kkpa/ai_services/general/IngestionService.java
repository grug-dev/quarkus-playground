package kkpa.ai_services.general;

import java.util.concurrent.CompletionStage;

public interface IngestionService {
  CompletionStage<Void> ingestBuildingData() throws kkpa.ai_services.general.DataIngestionException;
}
