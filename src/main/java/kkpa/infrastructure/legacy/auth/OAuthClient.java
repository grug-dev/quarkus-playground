package kkpa.infrastructure.legacy.auth;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

@Path("/")
public interface OAuthClient {

  /**
   * Makes a token request to the OAuth server
   *
   * @param authorization Authorization header value
   * @param formData Form data containing grant parameters
   * @return TokenResponseDto with token information
   */
  @POST
  @Path("oauth/token")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  TokenResponseDto getToken(
      @HeaderParam("Authorization") String authorization, MultivaluedMap<String, String> formData);
}
