package kkpa.infrastructure.legacy.auth;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "oauth")
public interface OAuthConfig {

  /** OAuth server URL */
  @WithName("server.url")
  @WithDefault("http://localhost:8585")
  String serverUrl();

  /** OAuth token endpoint path */
  @WithName("token.endpoint")
  @WithDefault("/oauth/token")
  String tokenEndpoint();

  /** OAuth client ID */
  @WithName("client.id")
  String clientId();

  /** OAuth client secret */
  @WithName("client.secret")
  String clientSecret();

  /** Default grant type */
  @WithName("grant.type")
  @WithDefault("password")
  String grantType();

  /** Default scope */
  @WithName("scope")
  @WithDefault("read write")
  String scope();
}
