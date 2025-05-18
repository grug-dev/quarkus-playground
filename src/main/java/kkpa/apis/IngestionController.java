package kkpa.apis;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import kkpa.ai_services.general.DataIngestionException;
import kkpa.ai_services.general.IngestionService;
import org.jboss.logging.Logger;

@Path("/api/ingestion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IngestionController {

  private static final Logger LOG = Logger.getLogger(IngestionController.class);

  private final IngestionService ingestionService;

  @Inject
  public IngestionController(IngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @GET
  @Path("/buildings")
  public CompletionStage<Response> ingestBuildingData() {
    LOG.info("Received request to ingest building data");

    try {
      return ingestionService
          .ingestBuildingData()
          .thenApply(unused -> Response.ok().entity("{\"status\": \"success\"}").build())
          .exceptionally(
              throwable -> {
                LOG.error("Error during building data ingestion", throwable);
                return Response.serverError()
                    .entity(
                        "{\"status\": \"error\", \"message\": \"" + throwable.getMessage() + "\"}")
                    .build();
              });
    } catch (DataIngestionException e) {
      LOG.error("Error initiating building data ingestion", e);
      return CompletableFuture.completedFuture(
          Response.serverError()
              .entity("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}")
              .build());
    }
  }
}
