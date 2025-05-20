package kkpa.infrastructure.legacy.transaction;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/** REST client interface for Transaction API */
@Path("/api")
public interface TransactionApiClient {

  @GET
  @Path("/transaction/transactionsByBuildingId")
  @Produces(MediaType.APPLICATION_JSON)
  List<TransactionDto> getTransactionsByBuildingId(@QueryParam("buildingId") Long buildingId);
}
