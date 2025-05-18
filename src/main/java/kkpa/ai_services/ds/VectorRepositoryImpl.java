package kkpa.ai_services.ds;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VectorRepositoryImpl implements VectorRepository {

  private static final Logger logger = LoggerFactory.getLogger(VectorRepositoryImpl.class);
  private final DataSource dataSource;
  private final ObjectMapper objectMapper;
  private final Executor executor = Executors.newFixedThreadPool(5);

  @Inject
  public VectorRepositoryImpl(DataSource dataSource, ObjectMapper objectMapper) {
    this.dataSource = dataSource;
    this.objectMapper = objectMapper;
  }

  @Override
  /**
   * Purpose of ON CONFLICT The ON CONFLICT clause in PostgreSQL (also called an "upsert" operation)
   * provides a way to handle inserting records that might conflict with existing data based on
   * unique constraints or primary keys. It's a powerful feature that allows you to specify what
   * should happen when a conflict occurs, rather than having the insert operation fail with an
   * error. Here's what it does:
   *
   * <p>Attempts the Insert First: PostgreSQL will first try to insert the new record. Detects
   * Conflicts: If the insertion would violate a unique constraint (like a primary key or unique
   * index), PostgreSQL detects this conflict. Takes Alternative Action: Instead of failing with an
   * error, PostgreSQL takes the action you specified after the ON CONFLICT clause.
   *
   * <p>There are two main forms of the ON CONFLICT clause: 1. ON CONFLICT DO NOTHING This simply
   * skips inserting rows that would cause conflicts: sqlINSERT INTO table_name (column1, column2,
   * ...) VALUES (value1, value2, ...) ON CONFLICT DO NOTHING 2. ON CONFLICT DO UPDATE This updates
   * the existing row with new values when a conflict occurs: sqlINSERT INTO table_name (column1,
   * column2, ...) VALUES (value1, value2, ...) ON CONFLICT (column1) DO UPDATE SET column2 =
   * EXCLUDED.column2, column3 = EXCLUDED.column3 The EXCLUDED refers to the row that would have
   * been inserted if there was no conflict. Benefits of Using ON CONFLICT
   *
   * <p>Atomic Operations: The entire operation is performed in a single atomic statement, reducing
   * the risk of race conditions. Simplified Code: Without ON CONFLICT, you'd need to:
   *
   * <p>Check if the record exists If it exists, perform an UPDATE If it doesn't exist, perform an
   * INSERT Handle any race conditions
   *
   * <p>Performance: A single database operation is generally more efficient than multiple
   * operations. Concurrency: When multiple processes are attempting to insert/update the same
   * records, ON CONFLICT handles the conflicts cleanly.
   *
   * <p>In your specific case, ON CONFLICT (building_id) DO UPDATE SET... means:
   *
   * <p>Try to insert a new building embedding If a record with the same building_id already exists,
   * update that record with the new values This ensures each building has exactly one embedding
   * record in the database
   */
  public CompletableFuture<Void> storeEmbeddings(List<BuildingEmbedding> embeddings) {
    return CompletableFuture.runAsync(
        () -> {
          String query =
              """
                INSERT INTO rag_store.props_embeddings
                (building_id, text, embedding, metadata)
                VALUES (?, ?, ?::vector, ?::jsonb)
                ON CONFLICT (building_id)
                DO UPDATE SET
                    text = EXCLUDED.text,
                    embedding = EXCLUDED.embedding,
                    metadata = EXCLUDED.metadata,
                    updated_at = CURRENT_TIMESTAMP
                """;

          try (Connection conn = dataSource.getConnection();
              PreparedStatement stmt = conn.prepareStatement(query)) {

            // Disable auto-commit for batch operations
            conn.setAutoCommit(false);

            for (BuildingEmbedding embedding : embeddings) {
              stmt.setLong(1, embedding.buildingId());
              stmt.setString(2, embedding.text());

              // Create the vector representation
              String vectorStr = convertToVectorString(embedding.embedding());
              PGobject vectorObject = new PGobject();
              vectorObject.setType("vector");
              try {
                vectorObject.setValue(vectorStr);
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
              stmt.setObject(3, vectorObject);

              // Create the JSONB metadata
              PGobject jsonbObject = new PGobject();
              jsonbObject.setType("jsonb");
              jsonbObject.setValue(embedding.metadata());
              stmt.setObject(4, jsonbObject);

              stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            logger.info("Successfully stored {} embeddings", results.length);
          } catch (SQLException e) {
            logger.error("Error storing embeddings", e);
            throw new RuntimeException("Error storing embeddings", e);
          }
        },
        executor);
  }

  private String convertToVectorString(float[] embedding) {
    // For PostgreSQL pgvector, the format is [n1,n2,n3,...]
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < embedding.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(embedding[i]);
    }
    sb.append("]");
    return sb.toString();
  }
}
