package ibm.gse.eda.inventory.api;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import ibm.gse.eda.inventory.domain.Inventory;
import io.quarkus.panache.common.Sort;

@Path("/inventory")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class InventoryResource {

    @GET
    public List<Inventory> get() {
        return Inventory.listAll(Sort.by("storeId"));
    }

    @GET
    @Path("{id}")
    public Inventory getSingle(@PathParam Long id) {
        Inventory entity = Inventory.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Inventory with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Transactional
    public Response create(Inventory Inventory) {
        if (Inventory.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        Inventory.persist();
        return Response.ok(Inventory).status(201).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Inventory update(@PathParam Long id, Inventory Inventory) {
        if (Inventory.storeId == null) {
            throw new WebApplicationException("Inventory storeId was not set on request.", 422);
        }
        if (Inventory.itemId == null) {
            throw new WebApplicationException("Inventory itemId was not set on request.", 422);
        }

        Inventory entity = Inventory.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Inventory with id of " + id + " does not exist.", 404);
        }

        entity.storeId = Inventory.storeId;
        entity.itemId = Inventory.itemId;
        entity.quantity = Inventory.quantity;
        entity.timestamp = (new Date().getTime());
        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam Long id) {
        Inventory entity = Inventory.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Inventory with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception exception) {
            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }
            return Response.status(code)
                    .entity(Json.createObjectBuilder().add("error", exception.getMessage()).add("code", code).build())
                    .build();
        }

    }
}