package kkpa.ai_services.general;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

@ApplicationScoped
public class QdrantAssistant {
  private static final Logger log = Logger.getLogger(QdrantAssistant.class);
  private GeneralAssistant assistant;
  private final ChatLanguageModel model;
  private final EmbeddingStore<TextSegment> documentStore;

  @Inject
  public QdrantAssistant(
      ChatLanguageModel model,
      @Named("documentStoreQdrant") EmbeddingStore<TextSegment> documentStore) {
    this.model = model;
    this.documentStore = documentStore;
    createAssistant();
    // this.moderationModel = moderationModel;
  }

  private void createAssistant() {
    ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
    EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    ContentRetriever contentRetriever =
        new EmbeddingStoreContentRetriever(documentStore, embeddingModel);

    assistant =
        AiServices.builder(GeneralAssistant.class)
            .chatLanguageModel(model)
            .chatMemory(chatMemory)
            .contentRetriever(contentRetriever)

            // .tools(new LegalDocumentTools())
            .build();
    log.info("GeneralAssistant service built");
  }

  public String freeChatWithMemory(String userMessage) {
    userMessage = userMessage.replaceAll("qdrant", "");
    log.info("QdrantAssistant Assistant Chat with user message: " + userMessage);
    try {
      AssistantResponse result = assistant.assistantResponse(userMessage);
      if (result == null || result.response().isEmpty() || result.equals("{}")) {
        return "I don't have enough information to answer that question properly.";
      }
      return result.response();
    } catch (Exception e) {
      log.error("Error in freeDevelopmentChatWithMemory", e);
      return "I'm not available right now. Please try again later.";
    }
  }
}
