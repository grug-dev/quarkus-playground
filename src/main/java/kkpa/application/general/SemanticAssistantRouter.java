package kkpa.application.general;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import kkpa.application.assistants.EAssistantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticAssistantRouter {

  private Logger log = LoggerFactory.getLogger(SemanticAssistantRouter.class);

  private final EmbeddingStore<TextSegment> docStore;
  private final EmbeddingModel embeddingModel;
  private final EmbeddingStore<TextSegment> propStore;

  @Inject
  public SemanticAssistantRouter(
      @Named("documentStore") EmbeddingStore<TextSegment> docStore,
      @Named("propertyStore") EmbeddingStore<TextSegment> propStore,
      EmbeddingModel embeddingModel) {
    this.docStore = docStore;
    this.propStore = propStore;
    this.embeddingModel = embeddingModel;
  }

  public EAssistantType searchAssistantType(String question) {
    log.info("Determining assistant type for question: {}", question);

    try {
      Embedding embeddedQuestion = embeddingModel.embed(question).content();

      EmbeddingSearchRequest searchRequest =
          EmbeddingSearchRequest.builder()
              .queryEmbedding(embeddedQuestion)
              .maxResults(3)
              .minScore(0.5)
              .build();

      // Search each store
      EmbeddingSearchResult<TextSegment> docResults = docStore.search(searchRequest);
      EmbeddingSearchResult<TextSegment> propResults = propStore.search(searchRequest);

      // Score comparison logic
      double bestDocScore =
          docResults.matches().stream().mapToDouble(EmbeddingMatch::score).max().orElse(0.0);

      double bestPropScore =
          propResults.matches().stream().mapToDouble(EmbeddingMatch::score).max().orElse(0.0);

      log.info("Best match scores - Document: {}, Property: {}", bestDocScore, bestPropScore);

      if (bestDocScore < 0.5 && bestPropScore < 0.5) {
        log.info("No strong matches found, using GENERAL assistant");
        return EAssistantType.GENERAL;
      }

      // Choose assistant with higher semantic relevance, but only if significantly better
      double significanceThreshold = 0.1; // 10% better to be considered significantly better

      EAssistantType selectedType;
      if (bestDocScore > bestPropScore * (1 + significanceThreshold)) {
        selectedType = EAssistantType.DOCUMENT;
      } else if (bestPropScore > bestDocScore * (1 + significanceThreshold)) {
        selectedType = EAssistantType.PROPERTY;
      } else {
        // If scores are close, choose based on absolute value
        selectedType =
            (bestDocScore > bestPropScore) ? EAssistantType.DOCUMENT : EAssistantType.PROPERTY;
      }

      log.info("Selected assistant type: {}", selectedType);
      return selectedType;
    } catch (Exception e) {
      log.error("Error determining assistant type: {}", e.getMessage(), e);
      return EAssistantType.GENERAL; // Fallback to GENERAL on error
    }
  }
}
