package ibn.gse.eda.stores.infrastructure;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import ibn.gse.eda.stores.domain.ItemSaleMessage;
import io.reactivex.Flowable;

@ApplicationScoped
public class ItemSaleGenerator {
    private long id = 0;
    private String[] stores = {"SC01","SF01","SF02","PT01","PT02","SEA01","NYC01","NYC02","LA01","LA02"};
    private Random random = new Random();

    private ItemSaleMessage buildNext(){
        ItemSaleMessage item = new ItemSaleMessage();
        item.id = id;
        item.storeName=stores[random.nextInt(stores.length)-1];
        item.itemCode = "IT0" + random.nextInt(9);
        item.quantity = random.nextInt(9);
        item.price = random.nextDouble() * 70;
        id++;

        return item;
    }
    
    @Outgoing("itemsales")
    public Flowable<ItemSaleMessage> generateItemSale() {
        return Flowable.interval(5,TimeUnit.SECONDS).map(tick -> buildNext());
    }
}