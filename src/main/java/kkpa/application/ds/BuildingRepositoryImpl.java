package kkpa.application.ds;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BuildingRepositoryImpl implements BuildingRepository {

  private static final Logger logger = LoggerFactory.getLogger(BuildingRepositoryImpl.class);
  private final DataSource dataSource;
  private final Executor executor = Executors.newFixedThreadPool(5);

  @Inject
  public BuildingRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public CompletableFuture<List<Building>> fetchBuildingsForEmbedding() {
    return CompletableFuture.supplyAsync(
        () -> {
          List<Building> buildings = new ArrayList<>();
          String query =
              """
                select b.id, b.buildingtype_id, b_type.name::json->'en' as building_type
                , b.city_id, c.name::json->'en' as city_name, cs.name::json->'en' as state,
                b.latitude, b.longitude
                from public.building b
                left join buildingtype b_type on b.buildingtype_id = b_type.id
                left join city c on b.city_id = c.id
                left join countrystate cs on c.countrystate_id = cs.id
                where b.status = 0
                and b.city_id is not null
                """;

          try (Connection conn = dataSource.getConnection();
              PreparedStatement stmt = conn.prepareStatement(query);
              ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
              Building building =
                  new Building(
                      rs.getLong("id"),
                      rs.getObject("buildingtype_id", Long.class),
                      rs.getString("building_type"),
                      rs.getObject("city_id", Long.class),
                      rs.getString("city_name"),
                      rs.getString("state"));
              buildings.add(building);
            }

            logger.info("Fetched {} buildings for embedding", buildings.size());
            return buildings;
          } catch (SQLException e) {
            logger.error("Error fetching buildings for embedding", e);
            throw new RuntimeException("Error fetching buildings for embedding", e);
          }
        },
        executor);
  }
}
