package kkpa.application.ds;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EmbeddingService {
  CompletableFuture<List<BuildingEmbedding>> generateEmbeddings(List<Building> buildings);
}
