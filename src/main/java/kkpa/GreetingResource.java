package kkpa;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import kkpa.infrastructure.legacy.auth.Credentials;
import kkpa.infrastructure.legacy.auth.OAuthService;

@Path("/hello")
public class GreetingResource {

  private final OAuthService oauthService;

  @Inject
  public GreetingResource(OAuthService oauthService) {
    this.oauthService = oauthService;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    System.out.println(
        "Token " + oauthService.getToken(new Credentials("panne@mangoreix.com", "Password321")));
    return "Hello from Quarkus REST";
  }

  @GET
  @Path("/{name}")
  @Produces(MediaType.TEXT_PLAIN)
  public String helloWithInput(@PathParam("name") String name) {
    return "Hello from Quarkus...Mister " + name;
  }
}
