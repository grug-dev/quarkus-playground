package kkpa.application.general;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import kkpa.application.assistants.AIServiceRouter;
import kkpa.application.assistants.PropertyAssistantService;
import kkpa.application.assistants.model.PropertyAIResponse;
import kkpa.application.assistants.property.formatters.PropertyResponseHtmlFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GeneralService {

  private static final Logger log = LoggerFactory.getLogger(GeneralService.class);
  private final ChatModel model;
  private final StreamingChatModel streamingModel;
  private final AIServiceRouter aiServiceRouter;

  private final PropertyResponseHtmlFormatter propertyResponseHtmlFormatter =
      new PropertyResponseHtmlFormatter();

  private final PropertyAssistantService propertyAssistantService;

  @Inject
  public GeneralService(
      ChatModel model,
      StreamingChatModel streamingModel,
      AIServiceRouter aiServiceRouter,
      PropertyAssistantService propertyAssistantService) {
    this.model = model;
    this.streamingModel = streamingModel;
    this.aiServiceRouter = aiServiceRouter;
    this.propertyAssistantService = propertyAssistantService;

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

  public String findAppropiateAssistantBasedOnMessage(String userMessage) {
    log.info("Finding appropriate assistant based on user message: " + userMessage);
    try {
      Object result = aiServiceRouter.processQuestion(userMessage);
      return transformResults(result);

    } catch (Exception e) {
      log.error("Error in findAppropiateAssistantBasedOnMessage", e);
      return "I'm not available right now. Please try again later.";
    }
  }

  public String propertyAssistant(String userMessage) {
    log.info("Property Assistant Chat with user message: " + userMessage);
    try {
      Object result = propertyAssistantService.processQuestion(userMessage);
      return transformResults(result);

    } catch (Exception e) {
      log.error("Error in propertyAssistant", e);
      return "I'm not available right now. Please try again later.";
    }
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
