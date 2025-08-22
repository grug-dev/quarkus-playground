package org.kkpa;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class MusicianAssistant {

  private final ChatLanguageModel model;

  @Inject
  public MusicianAssistant(ChatLanguageModel model) {
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

    Response<AiMessage> albums = model.generate(messages);
    String topThreeAlbums = albums.content().text();

    return new MusicianDTO(name, topThreeAlbums);
  }
}
