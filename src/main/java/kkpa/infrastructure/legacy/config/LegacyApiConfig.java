package kkpa.infrastructure.legacy.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "legacy")
public interface LegacyApiConfig {

  /** Legacy server base URL */
  @WithName("server.url")
  String serverUrl();

  /** Timeout for API requests in seconds */
  @WithName("timeout.seconds")
  @WithDefault("30")
  int timeoutSeconds();

  /** Default headers to include in all requests */
  @WithName("headers.accept")
  @WithDefault("application/json")
  String acceptHeader();
}
