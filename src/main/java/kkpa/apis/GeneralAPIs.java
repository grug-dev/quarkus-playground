package kkpa.apis;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import kkpa.ai_services.general.GeneralService;
import kkpa.ai_services.general.QdrantAssistant;
import kkpa.config.DocumentIngestor;
import org.jboss.logging.Logger;

@Path("/api/general/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GeneralAPIs {

  private static final Logger log = Logger.getLogger(GeneralAPIs.class);
  private final GeneralService generalService;
  private final DocumentIngestor documentIngestor;
  private final QdrantAssistant qdrantAssistant;

  @Inject
  public GeneralAPIs(
      GeneralService generalService,
      DocumentIngestor documentIngestor,
      QdrantAssistant qdrantAssistant) {
    this.documentIngestor = documentIngestor;
    this.generalService = generalService;
    this.qdrantAssistant = qdrantAssistant;
  }

  @GET
  @Path("/single-request")
  public String getBookAssistant() {
    String result = generalService.singleRequest(" Give me a short list of good sci-fi books.");

    return result != null ? result : "NOTHING";
  }

  @GET
  @Path("/token-stream/{userMsg}")
  public String testTokenStream(@PathParam("userMsg") String userMsg) {
    String result = generalService.generateMusicianBioByTokenStream(userMsg);

    return result != null ? result : "NOTHING";
  }

  @GET
  @Path("/ingest/docs")
  public String injectsDocs() throws Exception {
    documentIngestor.setupDocuments();
    return "Documents Injected";
  }

  @POST
  @Path("/java/dev/bot")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String processMessage(@FormParam("message") String userMsg) {
    log.info("Java Developer Chat Question: " + userMsg);
    // Get AI response from your service
    String aiResponse = null;

    if (userMsg.contains("qdrant")) {
      log.info("Qdrant Assistant Chat with user message: " + userMsg);
      aiResponse = qdrantAssistant.freeChatWithMemory(userMsg);
    } else {
      log.info("General Assistant Chat with user message: " + userMsg);
      aiResponse = generalService.freeChatWithMemory(userMsg);
    }

    // Return HTML snippet for the AI message
    return "<div class=\"message ai-message\">"
        + "<div class=\"message-content\">"
        + aiResponse
        + "</div>"
        + "<div class=\"message-time\">"
        + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        + "</div>"
        + "</div>";
  }
}
