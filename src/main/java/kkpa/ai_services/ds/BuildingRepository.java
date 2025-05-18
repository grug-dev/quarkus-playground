package kkpa.ai_services.ds;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BuildingRepository {
  CompletableFuture<List<Building>> fetchBuildingsForEmbedding();
}
