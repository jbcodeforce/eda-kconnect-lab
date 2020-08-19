package ibm.gse.eda.inventory.domain;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.reactive.messaging.annotations.Broadcast;

@ApplicationScoped
public class ItemAggregator {
    
    private static Logger LOG = Logger.getLogger(ItemAggregator.class.getName());
    
    /*
    @Incoming("item-channel")                            
    @Outgoing("inventory-channel")                         
    @Broadcast    
    */
    public Inventory processItem(Item item) {
            Inventory inventory = new Inventory();
            inventory.storeName = item.storeName;
            inventory.updateStock(item.itemCode, item.quantity);
            return inventory;
    }
}