package kkpa.ai_services.general;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
public class SemanticRouterStore {

  private final EmbeddingStore<TextSegment> docStore;
  private final EmbeddingModel embeddingModel;

  @Inject
  public SemanticRouterStore(
      @Named("documentStore") EmbeddingStore<TextSegment> docStore, EmbeddingModel embeddingModel) {
    this.docStore = docStore;
    this.embeddingModel = embeddingModel;
  }

  public EmbeddingStore<TextSegment> searchStore(String question) {
    Embedding embeddedQuestion = embeddingModel.embed(question).content();

    EmbeddingSearchRequest searchRequest =
        EmbeddingSearchRequest.builder()
            .queryEmbedding(embeddedQuestion)
            .maxResults(3)
            .minScore(0.5)
            .build();

    // Search each store
    EmbeddingSearchResult<TextSegment> docResults = docStore.search(searchRequest);
    // EmbeddingSearchResult<TextSegment> propResults = propStore.search(searchRequest);

    // Score comparison logic
    double bestDocScore =
        docResults.matches().stream().mapToDouble(EmbeddingMatch::score).max().orElse(0.0);

    if (bestDocScore < 0.5) {
      throw new RuntimeException("No relevant document found");
    }

    return docStore;

    /*
    double bestPropScore = propResults.matches().stream()
        .mapToDouble(EmbeddingMatch::score)
        .max().orElse(0.0);

     */

    // Choose store with higher semantic relevance
    /*
    EmbeddingSearchResult<TextSegment> selectedResult =
        (bestDocScore > bestPropScore) ? docResults : propResults;

     */

  }
}
