package kkpa.apis;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import kkpa.application.general.GeneralService;
import kkpa.application.general.QdrantAssistant;
import kkpa.config.DocumentIngestor;
import kkpa.infrastructure.legacy.auth.Credentials;
import kkpa.infrastructure.legacy.auth.OAuthService;
import kkpa.infrastructure.legacy.auth.RequestScopedTokenHolder;
import kkpa.infrastructure.legacy.auth.TokenResponse;
import org.jboss.logging.Logger;

@Path("/api/general/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GeneralAPIs {

  private static final Logger log = Logger.getLogger(GeneralAPIs.class);
  private final GeneralService generalService;
  private final DocumentIngestor documentIngestor;
  private final QdrantAssistant qdrantAssistant;
  private final RequestScopedTokenHolder tokenHolder;
  private final OAuthService oauthService;

  @Inject
  public GeneralAPIs(
      GeneralService generalService,
      DocumentIngestor documentIngestor,
      QdrantAssistant qdrantAssistant,
      RequestScopedTokenHolder tokenHolder,
      OAuthService oauthService) {
    this.documentIngestor = documentIngestor;
    this.generalService = generalService;
    this.qdrantAssistant = qdrantAssistant;
    this.tokenHolder = tokenHolder;
    this.oauthService = oauthService;
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
  @Path("/dev/bot")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String processMessage(
      @FormParam("message") String userMsg, @HeaderParam("Authorization") String authHeader) {
    log.info(" Chat Question: " + userMsg);
    // Get AI response from your service
    String aiResponse = null;

    // Extract and store token (optional here since the filter should do this)
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7).trim();
      tokenHolder.setAccessToken(token);
    } else {
      TokenResponse tokenResponse =
          oauthService.getToken(new Credentials("researcher@gmail.com", "Password123"));
      tokenHolder.setAccessToken(tokenResponse.accessToken());
    }

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
