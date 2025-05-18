package kkpa;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    return "Hello from Quarkus REST";
  }

  @GET
  @Path("/{name}")
  @Produces(MediaType.TEXT_PLAIN)
  public String helloWithInput(@PathParam("name") String name) {
    return "Hello from Quarkus...Mister " + name;
  }
}
