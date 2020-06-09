package ibm.gse.eda.mq.start;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import ibm.gse.eda.mq.start.dto.InventoryEntry;
import ibm.gse.eda.mq.start.infrastructure.mq.InventoryMessageConsumer;

@Path("/inventory")
@ApplicationScoped
@Produces("application/json")
public class MessageResource {

     @Inject
    public InventoryMessageConsumer messageConsumer;

    public MessageResource(){
    }

    @GET
    @Path("config")
    public String hello() {
        return messageConsumer.toJson();
    }

    @GET
    @Path("last")
    public InventoryEntry last() {
        return messageConsumer.getLastMessage();
    }
}