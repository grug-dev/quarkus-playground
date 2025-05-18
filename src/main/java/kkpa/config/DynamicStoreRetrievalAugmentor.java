package kkpa.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DynamicStoreRetrievalAugmentor implements RetrievalAugmentor {

  private static final Logger log = LoggerFactory.getLogger(DynamicStoreRetrievalAugmentor.class);

  @Inject EmbeddingModel embeddingModel;

  @Inject
  @Named("documentStore")
  EmbeddingStore<TextSegment> docStore;

  @Inject
  @Named("propertyStore")
  EmbeddingStore<TextSegment> propertyStore;

  /** Implementation of the new method from version 1.0.0-beta1 */
  @Override
  public AugmentationResult augment(AugmentationRequest augmentationRequest) {
    if (!(augmentationRequest.chatMessage() instanceof UserMessage)) {
      log.warn(
          "Augmentation request contains non-UserMessage: "
              + augmentationRequest.chatMessage().getClass().getSimpleName());
      return AugmentationResult.builder().chatMessage(augmentationRequest.chatMessage()).build();
    }

    UserMessage userMessage = (UserMessage) augmentationRequest.chatMessage();
    UserMessage augmentedMessage = augment(userMessage, augmentationRequest.metadata());

    return AugmentationResult.builder()
        .chatMessage(augmentedMessage)
        // In beta1, you can also include the retrieved contents in the result
        .contents(getRetrievedContents(userMessage.singleText()))
        .build();
  }

  /** Implementation of the deprecated method (still required in beta1) */
  @Override
  public UserMessage augment(UserMessage userMessage, Metadata metadata) {
    String question = userMessage.singleText();
    log.info("Augmenting message: {}", question);

    // Select the appropriate store based on the question
    EmbeddingStore<TextSegment> selectedStore = selectStore(question);
    log.info("Selected store: {}", selectedStore.getClass().getSimpleName());

    // Retrieve relevant content from the selected store
    List<TextSegment> relevantSegments = retrieveRelevantContent(question, selectedStore);

    if (relevantSegments.isEmpty()) {
      log.info("No relevant content found for: {}", question);
      return userMessage;
    }

    // Create context from relevant segments
    String context = buildContext(relevantSegments);

    // Augment the user message with the context
    return UserMessage.from(
        "I have the following context that might help answer my question:\n\n"
            + context
            + "\n\n"
            + "Based on this context, please answer my question: "
            + question);
  }

  /** Helper method for AugmentationResult to include retrieved contents */
  private List<Content> getRetrievedContents(String question) {
    EmbeddingStore<TextSegment> selectedStore = selectStore(question);
    List<TextSegment> segments = retrieveRelevantContent(question, selectedStore);

    return segments.stream()
        .map(s -> Content.from(TextSegment.from(s.text(), s.metadata())))
        .collect(Collectors.toList());
  }

  private EmbeddingStore<TextSegment> selectStore(String question) {

    // More sophisticated embedding-based selection
    Embedding questionEmbedding = embeddingModel.embed(question).content();

    // Compare with store descriptions
    String docStoreDesc = "Vinta Store Documents";
    String propertyStoreDesc =
        "Property listings, Buildings information, real estate data, house information";

    double docSimilarity =
        calculateSimilarity(questionEmbedding, embeddingModel.embed(docStoreDesc).content());

    double propertySimilarity =
        calculateSimilarity(questionEmbedding, embeddingModel.embed(propertyStoreDesc).content());

    log.info("Store similarity scores: doc={}, property={}", docSimilarity, propertySimilarity);

    if (propertyStore == null) {
      return docStore;
    }

    // Return the store with higher similarity
    return docSimilarity > propertySimilarity ? docStore : propertyStore;
  }

  private List<TextSegment> retrieveRelevantContent(
      String question, EmbeddingStore<TextSegment> store) {
    try {
      Embedding questionEmbedding = embeddingModel.embed(question).content();

      EmbeddingSearchRequest searchRequest =
          EmbeddingSearchRequest.builder()
              .queryEmbedding(questionEmbedding)
              .maxResults(3)
              .minScore(0.5)
              .build();

      EmbeddingSearchResult<TextSegment> searchResult = store.search(searchRequest);

      return searchResult.matches().stream()
          .map(EmbeddingMatch::embedded)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error retrieving content from store: " + e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private String buildContext(List<TextSegment> segments) {
    StringBuilder context = new StringBuilder();

    for (int i = 0; i < segments.size(); i++) {
      TextSegment segment = segments.get(i);
      context.append("Source ").append(i + 1).append(": ");

      // If metadata contains source info, include it
      if (segment.metadata() != null && segment.metadata().containsKey("source")) {
        context.append(segment.metadata().getString("source")).append("\n");
      } else {
        context.append("\n");
      }

      context.append(segment.text()).append("\n\n");
    }

    return context.toString();
  }

  private double calculateSimilarity(Embedding e1, Embedding e2) {
    List<Float> v1 = e1.vectorAsList();
    List<Float> v2 = e2.vectorAsList();

    double dotProduct = 0.0;
    double norm1 = 0.0;
    double norm2 = 0.0;

    for (int i = 0; i < v1.size(); i++) {
      dotProduct += v1.get(i) * v2.get(i);
      norm1 += v1.get(i) * v1.get(i);
      norm2 += v2.get(i) * v2.get(i);
    }

    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
  }
}
