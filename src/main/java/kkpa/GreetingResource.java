package kkpa;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import kkpa.infrastructure.legacy.account.AccountLegacyService;

@Path("/hello")
public class GreetingResource {

  private final AccountLegacyService accountLegacyService;

  @Inject
  public GreetingResource(AccountLegacyService accountLegacyService) {
    this.accountLegacyService = accountLegacyService;
  }

  @GET
  @Path("/{name}")
  @Produces(MediaType.TEXT_PLAIN)
  public String helloWithInput(@PathParam("name") String name) {
    return "Hello from Quarkus...Mister " + name;
  }
}
