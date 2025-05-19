package kkpa.application.ds;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DatabaseConfig {

  @ConfigProperty(name = "quarkus.datasource.username")
  String dbUsername;

  @ConfigProperty(name = "quarkus.datasource.password")
  String dbPassword;

  @ConfigProperty(name = "quarkus.datasource.jdbc.url")
  String dbUrl;

  @ConfigProperty(name = "quarkus.datasource.dbhost")
  String dbHost;

  @ConfigProperty(name = "quarkus.datasource.dbname")
  String dbName;

  @ConfigProperty(name = "quarkus.datasource.dbport")
  int dbPort;
}
