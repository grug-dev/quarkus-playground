package kkpa.ai_services.general;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/** Needs to be implemented by an AI Service. */
public interface GeneralAssistant {
  String singleRequest(String userMessage);

  @SystemMessage("You are an expert in musicians biography")
  @UserMessage("Write a short biography about {{musician}}.")
  TokenStream generateMusicianBioByTokenStream(@V("musician") String musicianName);

  @SystemMessage(
      """
    You are an expert of the company Vintage Store.
    Focus on the customer and answer the questions.
    Keep your answers short and to the point.
    If you don't know the answer, say 'I don't know'.
    """)
  @UserMessage("Here is the customer's question: {{userMessage}}")
  AssistantResponse assistantResponse(@V("userMessage") String userMessage);
}
