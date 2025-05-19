package kkpa.application.general;

import java.util.concurrent.CompletionStage;

public interface IngestionService {
  CompletionStage<Void> ingestBuildingData() throws kkpa.application.general.DataIngestionException;
}
