package ibm.gse.eda;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import ibm.gse.eda.stores.domain.ItemSaleMessage;
import ibm.gse.eda.stores.infrastructure.ItemSaleGenerator;

@Path("/sales")

public class StoreResource {

    @Inject
    ItemSaleGenerator generator;

    @GET
    @Produces("application/json")
    public ItemSaleMessage lastSale() {
        return generator.getLastItem();
    }

    @POST
    @Path("/start/{records}")
    public Response startSendingMessage(@PathParam int records) {
        generator.start(records);
        return Response.ok().status(201).build();
    }
}