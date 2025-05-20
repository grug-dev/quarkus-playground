package kkpa.infrastructure.legacy.account;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** REST client interface for Account API */
@Path("/api")
public interface AccountApiClient {

  @GET
  @Path("/account")
  @Produces(MediaType.APPLICATION_JSON)
  AccountDto getAccountInfo();
}
