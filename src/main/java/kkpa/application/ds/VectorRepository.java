package kkpa.application.ds;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface VectorRepository {

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
   *
   * @param embeddings
   * @return
   */
  CompletableFuture<Void> storeEmbeddings(List<BuildingEmbedding> embeddings);
}
