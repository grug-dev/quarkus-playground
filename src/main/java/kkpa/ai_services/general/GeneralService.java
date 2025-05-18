package kkpa.ai_services.general;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import kkpa.config.DynamicStoreRetrievalAugmentor;
import org.jboss.logging.Logger;

@ApplicationScoped
public class GeneralService {

  private static final Logger log = Logger.getLogger(GeneralService.class);
  private final ChatLanguageModel model;
  private final StreamingChatLanguageModel streamingModel;
  private final EmbeddingStore<TextSegment> documentStore;
  private final EmbeddingModel embeddingModel;
  private final DynamicStoreRetrievalAugmentor retrievalAugmentor;
  private GeneralAssistant assistant;

  // private final ModerationModel moderationModel;

  @Inject
  public GeneralService(
      ChatLanguageModel model,
      StreamingChatLanguageModel streamingModel,
      DynamicStoreRetrievalAugmentor retrievalAugmentor,
      @Named("documentStore") EmbeddingStore<TextSegment> documentStore,
      EmbeddingModel embeddingModel) {
    this.model = model;
    this.streamingModel = streamingModel;
    this.documentStore = documentStore;
    this.embeddingModel = embeddingModel;
    this.retrievalAugmentor = retrievalAugmentor;
    createAssistant();
    // this.moderationModel = moderationModel;
  }

  private void createAssistant() {
    ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);
    log.info("Chat memory created");
    ContentRetriever contentRetriever =
        new EmbeddingStoreContentRetriever(documentStore, embeddingModel);

    assistant =
        AiServices.builder(GeneralAssistant.class)
            .chatLanguageModel(model)
            .chatMemory(chatMemory)
            .retrievalAugmentor(retrievalAugmentor)

            // .tools(new LegalDocumentTools())
            .build();
    log.info("GeneralAssistant service built");
  }

  public String singleRequest(String userMessage) {
    log.info("Single Request Assistant is called with " + userMessage);
    GeneralAssistant assistant = AiServices.create(GeneralAssistant.class, model);
    return assistant.singleRequest(userMessage);
  }

  public String generateMusicianBioByTokenStream(String userMsg) {
    log.info("Token Stream example");
    GeneralAssistant assistant = AiServices.create(GeneralAssistant.class, streamingModel);

    // Use CompletableFuture to handle the asynchronous result
    CompletableFuture<String> resultFuture = new CompletableFuture<>();

    assistant
        .generateMusicianBioByTokenStream(userMsg)
        .onPartialResponse(
            partialResponse -> {
              log.info("Partial Response: " + partialResponse);
            })
        .onRetrieved(
            retrievedContents -> {
              log.info("Retrieved Contents: " + retrievedContents);
            })
        .onToolExecuted(
            toolExecution -> {
              log.info("Tool Execution: " + toolExecution);
            })
        .onError(
            throwable -> {
              log.error("Error: " + throwable);
              resultFuture.completeExceptionally(throwable); // Complete with exception
            })
        .onCompleteResponse(
            complete -> {
              String finalResponse = complete.aiMessage().text();
              log.info("Complete: " + finalResponse);
              resultFuture.complete(finalResponse); // Complete the future with result
            })
        .start();

    try {
      // Block and wait for the result
      return resultFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Failed to get response", e);
      throw new RuntimeException("Failed to generate musician bio", e);
    }
  }

  public String freeChatWithMemory(String userMessage) {
    log.info("General Assistant Chat with user message: " + userMessage);
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
