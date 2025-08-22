package kkpa.infrastructure.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import kkpa.application.assistants.PropertyAssistantService;
import kkpa.infrastructure.legacy.auth.Credentials;
import kkpa.infrastructure.legacy.auth.OAuthService;
import kkpa.infrastructure.legacy.auth.RequestScopedTokenHolder;
import kkpa.infrastructure.legacy.auth.TokenResponse;
import org.jboss.logging.Logger;

@Path("/api/properties-bot/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PropertiesChatBotResource {

  private final PropertyAssistantService propertyAssistantService;
  private final RequestScopedTokenHolder tokenHolder;
  private final OAuthService oauthService;

  private static final Logger log = Logger.getLogger(GeneralAPIs.class);

  @Inject
  public PropertiesChatBotResource(
      PropertyAssistantService propertyAssistantService,
      RequestScopedTokenHolder tokenHolder,
      OAuthService oauthService) {
    this.propertyAssistantService = propertyAssistantService;
    this.tokenHolder = tokenHolder;
    this.oauthService = oauthService;
  }

  @POST
  @Path("/chat")
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

    aiResponse = propertyAssistantService.processQuestion(userMsg);

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
