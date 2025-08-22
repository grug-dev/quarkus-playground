package kkpa.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LLMModelFactory {

  private Logger log = Logger.getLogger(LLMModelFactory.class);

  @ConfigProperty(name = "ollama.base.url")
  String ollamaBaseUrl;

  @ConfigProperty(name = "ollama.model.name")
  String modelName;

  @ConfigProperty(name = "ollama.timeout.minutes", defaultValue = "5")
  int timeoutMinutes;

  @ConfigProperty(name = "ollama.temperature", defaultValue = "0.1")
  double temperature;

  @Produces
  @ApplicationScoped
  @Named("regularChatModel")
  public ChatModel createChatModel() {
    log.info("Creating a new Ollama Chat Language Model...");
    log.info("ollama.base.url: " + ollamaBaseUrl);
    log.info("ollama.model.name: " + modelName);
    log.info("ollama.timeout.minutes: " + timeoutMinutes);
    log.info("ollama.temperature: " + temperature);

    return OllamaChatModel.builder()
        .baseUrl(ollamaBaseUrl)
        .modelName(modelName)
        .temperature(temperature)
        .logRequests(true)
        .logResponses(true)
        .timeout(Duration.ofMinutes(timeoutMinutes))
        .responseFormat(ResponseFormat.JSON)
        .build();
  }

  @Produces
  @ApplicationScoped
  @Named("regularEmbeddingModel")
  @Default
  public EmbeddingModel createEmbeddingModel() {
    return OllamaEmbeddingModel.builder()
        .modelName("nomic-embed-text")
        .baseUrl(ollamaBaseUrl)
        .logRequests(true)
        .logResponses(true)
        .timeout(Duration.ofMinutes(timeoutMinutes))
        .build();
  }

  @Produces
  @ApplicationScoped
  @Named("streamingChatModel")
  public StreamingChatModel createStreamingChatModel() {
    log.info("Creating a new Ollama Streaming Chat Language Model...");
    log.info("ollama.base.url: " + ollamaBaseUrl);
    log.info("ollama.model.name: " + modelName);
    log.info("ollama.timeout.minutes: " + timeoutMinutes);
    log.info("ollama.temperature: " + temperature);

    return OllamaStreamingChatModel.builder()
        .baseUrl(ollamaBaseUrl)
        .modelName(modelName)
        .logRequests(true)
        .logResponses(true)
        .temperature(temperature)
        .timeout(Duration.ofMinutes(timeoutMinutes))
        .build();
  }
}
