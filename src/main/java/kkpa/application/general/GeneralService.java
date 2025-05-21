package kkpa.application.general;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import kkpa.application.assistants.AIServiceRouter;
import kkpa.application.assistants.PropertyAIResponse;
import kkpa.application.assistants.property.formatters.PropertyResponseHtmlFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GeneralService {

  private static final Logger log = LoggerFactory.getLogger(GeneralService.class);
  private final ChatLanguageModel model;
  private final StreamingChatLanguageModel streamingModel;
  private final AIServiceRouter aiServiceRouter;

  private final PropertyResponseHtmlFormatter propertyResponseHtmlFormatter =
      new PropertyResponseHtmlFormatter();

  // private final ModerationModel moderationModel;

  @Inject
  public GeneralService(
      ChatLanguageModel model,
      StreamingChatLanguageModel streamingModel,
      AIServiceRouter aiServiceRouter) {
    this.model = model;
    this.streamingModel = streamingModel;
    this.aiServiceRouter = aiServiceRouter;

    // this.moderationModel = moderationModel;
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
      Object result = aiServiceRouter.processQuestion(userMessage);

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

    } catch (Exception e) {
      log.error("Error in freeDevelopmentChatWithMemory", e);
      return "I'm not available right now. Please try again later.";
    }
  }
}
