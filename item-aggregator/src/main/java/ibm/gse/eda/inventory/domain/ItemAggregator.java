package ibm.gse.eda.inventory.domain;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.reactive.messaging.annotations.Broadcast;

@ApplicationScoped
public class ItemAggregator {
    private static Logger LOG = Logger.getLogger(ItemAggregator.class.getName());
    @Incoming("item-channel")                            
    @Outgoing("inventory-channel")                         
    @Broadcast    
    public Inventory processItem(Item item) {
            Inventory inventory = new Inventory();
            inventory.itemCode = item.itemCode;
            inventory.price = item.price;
            inventory.quantity = item.quantity;
            inventory.storeName = item.storeName;
            LOG.info("Inventory created for " +  inventory.itemCode + " in " + inventory.storeName);
            return inventory;
    }
}