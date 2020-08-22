package ibm.gse.eda.inventory.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ibm.gse.eda.inventory.infrastructure.InteractiveQueries;
import ibm.gse.eda.inventory.infrastructure.InventoryQueryResult;
import ibm.gse.eda.inventory.infrastructure.PipelineMetadata;

@ApplicationScoped
@Path("/inventory")
public class InventoryResource {
    
    @Inject
    public InteractiveQueries queries;

    @GET
    @Path("/store/{storeID}/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStock(@PathParam("storeID") String storeID, @PathParam("itemID") String itemID) {
        InventoryQueryResult result = queries.getStoreStock(storeID,itemID);
        if (result.getResult().isPresent()) {
            return Response.ok(result.getResult().get()).build();
        } else if (result.getHost().isPresent()) {
            URI otherUri = getStoreOtherUri(result.getHost().get(), result.getPort().getAsInt(), storeID, itemID);
            return Response.seeOther(otherUri).build();
        } else {
            return Response.status(Status.NOT_FOUND.getStatusCode(), "No data found for store " + storeID + " item " + itemID).build();
        }
    }

    @GET
    @Path("/meta-data")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PipelineMetadata> getMetaData() {
        return queries.getStockStoreMetaData();
    }
    
    @GET
    @Path("/item/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Integer getGlobalItem(@PathParam("itemID") String itemID) {
        return 0;
        
    }

    private URI getStoreOtherUri(String host, int port, String storeID, String itemID) {
        try {
            return new URI("http://" + host + ":" + port + "/inventory/store/" + storeID + "/" + itemID);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}