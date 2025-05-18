package kkpa.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import java.net.URI;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmbeddingStoreFactory {

  private static final Logger log = Logger.getLogger(EmbeddingStoreFactory.class);

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

  @ConfigProperty(name = "pgvector.dimensions")
  int dimensions;

  @ConfigProperty(name = "quarkus.datasource.dbport")
  int dbPort;

  @Produces
  @ApplicationScoped
  @Named("documentStore")
  @Default
  public EmbeddingStore<TextSegment> setupDocumentStore() {
    log.info("Creating a new Embedding Store...");
    return PgVectorEmbeddingStore.builder()
        .table("rag_store.docs_embeddings")
        .dimension(dimensions)
        .database(dbName)
        .password(dbPassword)
        .user(dbUsername)
        .port(dbPort)
        .useIndex(false)
        .host(dbHost)
        .build();
  }

  @Produces
  @ApplicationScoped
  @Named("propertyStore")
  @Default
  public EmbeddingStore<TextSegment> setupPropertyStore() {
    log.info("Creating Property Embedding Store...");
    return PgVectorEmbeddingStore.builder()
        .table("rag_store.props_embeddings")
        .dimension(dimensions)
        .database(dbName)
        .password(dbPassword)
        .user(dbUsername)
        .port(dbPort)
        .useIndex(false)
        .host(dbHost)
        .build();
  }

  @Produces
  @ApplicationScoped
  @Named("documentStoreQdrant")
  public EmbeddingStore<TextSegment> embeddingStore() throws Exception {
    final String INDEX_NAME = "VintageStoreIndex";
    final String QDRANT_URL = "http://localhost:6334";

    String qdrantHostname = new URI(QDRANT_URL).getHost();
    int qdrantPort = new URI(QDRANT_URL).getPort();

    QdrantGrpcClient.Builder grpcClientBuilder =
        QdrantGrpcClient.newBuilder(qdrantHostname, qdrantPort, false);
    QdrantClient qdrantClient = new QdrantClient(grpcClientBuilder.build());
    QdrantEmbeddingStore result =
        QdrantEmbeddingStore.builder().client(qdrantClient).collectionName(INDEX_NAME).build();
    log.info("QDrant Embedding Store created " + result);

    return result;
  }
}
