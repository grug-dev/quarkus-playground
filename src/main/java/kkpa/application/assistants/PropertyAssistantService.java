package kkpa.application.assistants;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import kkpa.application.assistants.model.PropertyAIResponse;
import kkpa.application.assistants.property.formatters.PropertyResponseHtmlFormatter;
import kkpa.application.general.AssistantResponse;
import kkpa.config.PropertyRetrievalAugmentor;
import kkpa.tools.PropertyTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PropertyAssistantService {
  private static final Logger log = LoggerFactory.getLogger(PropertyAssistantService.class);
  private final PropertyAssistant propertyAssistant;
  private final PropertyResponseHtmlFormatter propertyResponseHtmlFormatter =
      new PropertyResponseHtmlFormatter();

  private final PropertyTools propertyTools;

  @Inject
  public PropertyAssistantService(
      ChatModel model,
      EmbeddingModel embeddingModel,
      PropertyRetrievalAugmentor retrievalAugmentor,
      @Named("propertyStore") EmbeddingStore<TextSegment> propertyStore,
      ToolsFactory toolsFactory) {
    this.propertyTools = toolsFactory.createPropertyTools();

    // Create property assistant with property-specific instructions
    ChatMemory propertyChatMemory = MessageWindowChatMemory.withMaxMessages(20);
    ContentRetriever propertyRetriever =
        EmbeddingStoreContentRetriever.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(propertyStore)
            .minScore(0.5)
            .build();

    log.info("Creating Property Assistant");

    this.propertyAssistant =
        AiServices.builder(PropertyAssistant.class)
            .chatModel(model)
            .chatMemory(propertyChatMemory)
            .contentRetriever(propertyRetriever)
            .tools(propertyTools)
            .build();
  }

  public String processQuestion(String question) {
    return transformResults(propertyAssistant.chat(question));
  }

  private String transformResults(Object result) {
    if (result == null) {
      return "I don't have enough information to answer that question properly.";
    }

    if (result instanceof AssistantResponse) {
      AssistantResponse aiResponse = ((AssistantResponse) result);
      if (aiResponse.response().isEmpty() || aiResponse.response().equals("{}")) {
        return "I don't have enough information to answer that question properly.";
      }
      return aiResponse.response();
    }
    if (result instanceof PropertyAIResponse aiResponse) {
      log.info("AI Response: {}", aiResponse);
      return propertyResponseHtmlFormatter.formatToHtml(aiResponse);
    }

    return result.toString();
  }
}
