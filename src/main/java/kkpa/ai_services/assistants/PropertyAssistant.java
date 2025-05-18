package kkpa.ai_services.assistants;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import java.util.List;
import kkpa.ai_services.ds.BuildingEmbedding;

public interface PropertyAssistant {

  @SystemMessage(
      "You are a specialized real estate assistant that helps with questions about properties/buildings in Brazil. "
          + "Always include property/building specifications in your answers when available. "
          + "Format prices in a consistent manner with the currency symbol. "
          + "You should either:\n"
          + "1. Answer user questions about a specific property using available information.\n"
          + "2. Return a list of properties/buildings in a well-formatted JSON array if the question is about multiple or matching listings.\n\n"
          + "If the information isn't in the retrieved content, say you don't have that information.\n\n"
          + "NEVER make up information. Only use retrieved content."
          + "ONLY return information from the retrieved content. NEVER make up information.")
  @UserMessage("Here is the customer's question: {{userMessage}}")
  String chat(String userMessage);

  @SystemMessage(
      "You are a specialized real estate assistant that helps with questions about properties/buildings in Brazil. "
          + "Return a list of properties matching the query as a structured JSON array.")
  @UserMessage("{{userMessage}}")
  List<BuildingEmbedding> listProperties(String userMessage);
}
