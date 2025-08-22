package org.kkpa;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class MusicianAssistant {

  private final ChatModel model;

  @Inject
  public MusicianAssistant(ChatModel model) {
    this.model = model;
  }

  public MusicianDTO generateTopThreeAlbums(String name) {
    SystemMessage systemMsg =
        SystemMessage.from(
            """
            You are an expert in Jazz music.
            Reply with only the names of the artists, albums, etc.
            Be very concise.
            If a list is given, separate the items with commas.
            """);
    UserMessage userMsg = UserMessage.from(String.format("Only list the top 3 albums of %s", name));
    List<ChatMessage> messages = List.of(systemMsg, userMsg);

    ChatResponse response = model.chat(messages);
    String topThreeAlbums = response.aiMessage().text();

    return new MusicianDTO(name, topThreeAlbums);
  }
}
