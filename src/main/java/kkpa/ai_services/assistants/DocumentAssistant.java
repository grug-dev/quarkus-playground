package kkpa.ai_services.assistants;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DocumentAssistant {
  @SystemMessage(
      """
    You are an expert of the company Vintage Store.
    Focus on the customer and answer the questions.
    Keep your answers short and to the point.
    If you don't know the answer, say 'I don't know'.
    """)
  @UserMessage("Here is the customer's question: {{userMessage}}")
  String chat(String userMessage);
}
