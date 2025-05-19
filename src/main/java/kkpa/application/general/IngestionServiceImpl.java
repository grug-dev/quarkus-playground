package kkpa.application.general;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import kkpa.application.ds.BuildingRepository;
import kkpa.application.ds.EmbeddingService;
import kkpa.application.ds.VectorRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class IngestionServiceImpl implements IngestionService {

  private static final Logger LOG = Logger.getLogger(IngestionServiceImpl.class);

  private final BuildingRepository buildingRepository;
  private final EmbeddingService embeddingService;
  private final VectorRepository vectorRepository;

  @Inject
  public IngestionServiceImpl(
      BuildingRepository buildingRepository,
      EmbeddingService embeddingService,
      VectorRepository vectorRepository) {
    this.buildingRepository = buildingRepository;
    this.embeddingService = embeddingService;
    this.vectorRepository = vectorRepository;
  }

  @Override
  public CompletionStage<Void> ingestBuildingData() throws DataIngestionException {
    LOG.info("Starting building data ingestion process");

    return buildingRepository
        .fetchBuildingsForEmbedding()
        .thenCompose(
            buildings -> {
              LOG.info("Fetched " + buildings.size() + " buildings for embedding");
              return embeddingService.generateEmbeddings(buildings);
            })
        .thenCompose(
            embeddings -> {
              LOG.info("Generated " + embeddings.size() + " embeddings");
              return vectorRepository.storeEmbeddings(embeddings);
            })
        .exceptionally(
            throwable -> {
              LOG.error("Building data ingestion failed", throwable);
              CompletableFuture<Void> failedFuture = new CompletableFuture<>();
              failedFuture.completeExceptionally(
                  new DataIngestionException("Building data ingestion failed", throwable));
              throw new RuntimeException(throwable);
            });
  }
}
