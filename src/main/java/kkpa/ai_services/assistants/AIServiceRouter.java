package kkpa.ai_services.assistants;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import kkpa.ai_services.general.GeneralAssistant;
import kkpa.ai_services.general.SemanticAssistantRouter;
import kkpa.config.PropertyRetrievalAugmentor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AIServiceRouter {

  private static final Logger log = LoggerFactory.getLogger(AIServiceRouter.class);

  private final DocumentAssistant documentAssistant;
  private final PropertyAssistant propertyAssistant;
  private final GeneralAssistant generalAssistant;
  private final EmbeddingModel embeddingModel;

  private final SemanticAssistantRouter semanticAssistantRouter;

  @Inject
  public AIServiceRouter(
      ChatLanguageModel model,
      EmbeddingModel embeddingModel,
      PropertyRetrievalAugmentor retrievalAugmentor,
      @Named("documentStore") EmbeddingStore<TextSegment> docStore,
      @Named("propertyStore") EmbeddingStore<TextSegment> propertyStore) {

    this.embeddingModel = embeddingModel;

    // Create document assistant with document-specific instructions
    ChatMemory regularChatMemory = MessageWindowChatMemory.withMaxMessages(20);
    ContentRetriever docRetriever = new EmbeddingStoreContentRetriever(docStore, embeddingModel);

    this.documentAssistant =
        AiServices.builder(DocumentAssistant.class)
            .chatLanguageModel(model)
            .chatMemory(regularChatMemory)
            .build();

    // Create property assistant with property-specific instructions
    ChatMemory propertyChatMemory = MessageWindowChatMemory.withMaxMessages(20);
    ContentRetriever propertyRetriever =
        EmbeddingStoreContentRetriever.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(propertyStore)
            .minScore(0.5)
            .maxResults(5)
            .build();

    this.propertyAssistant =
        AiServices.builder(PropertyAssistant.class)
            .chatLanguageModel(model)
            .chatMemory(propertyChatMemory)
            .retrievalAugmentor(retrievalAugmentor)
            .build();

    this.generalAssistant =
        AiServices.builder(GeneralAssistant.class)
            .chatLanguageModel(model)
            .chatMemory(regularChatMemory)
            .build();

    semanticAssistantRouter = new SemanticAssistantRouter(docStore, propertyStore, embeddingModel);
  }

  /** Routes the user question to the appropriate assistant based on content */
  public Object processQuestion(String question) {
    // Determine which assistant should handle this question
    EAssistantType assistantType = semanticAssistantRouter.searchAssistantType(question);

    if (assistantType == null || assistantType == EAssistantType.GENERAL) {
      log.info("Routing to general assistant: {}", question);
      return generalAssistant.chat(question);
    }

    if (assistantType == EAssistantType.PROPERTY) {
      log.info("Routing to property assistant: {}", question);
      return propertyAssistant.chat(question);
    } else if (assistantType == EAssistantType.DOCUMENT) {
      log.info("Routing to document assistant: {}", question);
      return documentAssistant.chat(question);
    }

    return "I'm sorry, I don't know how to answer your question.";
  }
}
