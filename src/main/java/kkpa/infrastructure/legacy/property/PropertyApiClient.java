package kkpa.infrastructure.legacy.property;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** REST client interface for Property API */
@Path("/api")
public interface PropertyApiClient {

  @GET
  @Path("/building/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  PropertyDto getPropertyById(@PathParam("id") Long id);
}
