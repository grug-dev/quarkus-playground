package kkpa.apis;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import kkpa.musician.MusicianAssistant;
import kkpa.musician.MusicianDTO;

@Path("/api/musicians")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MusicianResource {

  private final MusicianAssistant musicianAssistant;

  @Inject
  public MusicianResource(MusicianAssistant musicianAssistant) {
    this.musicianAssistant = musicianAssistant;
  }

  @GET
  @Path("/{name}/top-albums")
  public Response getTopAlbums(@PathParam("name") String name) {
    try {
      MusicianDTO musician = musicianAssistant.generateTopThreeAlbums(name);
      return Response.ok(musician).build();
    } catch (Exception e) {
      return Response.serverError()
          .entity(new ErrorResponse("Failed to generate top albums: " + e.getMessage()))
          .build();
    }
  }

  // You might want to add more endpoints here

  // Simple error response class
  public static class ErrorResponse {
    private final String message;

    public ErrorResponse(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
