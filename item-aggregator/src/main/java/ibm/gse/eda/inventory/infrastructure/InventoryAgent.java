package ibm.gse.eda.inventory.infrastructure;

import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import ibm.gse.eda.inventory.domain.Inventory;
import ibm.gse.eda.inventory.domain.Item;
import io.quarkus.kafka.client.serialization.JsonbSerde;

public class InventoryAgent {
    
    @Inject
    @ConfigProperty(name="mp.messaging.incoming.item-channel.topic")
    public String itemSoldTopicName;

    @Inject
    @ConfigProperty(name="mp.messaging.incoming.inventory-channel.topic")
    public String inventoryStockTopicName;

    
    public String storeName = "StoreStock";

    private JsonbSerde<Item> itemSerde = new JsonbSerde<>(Item.class);
    private JsonbSerde<Inventory> inventorySerde = new JsonbSerde<>(Inventory.class);
    
    private KeyValueBytesStoreSupplier storeStockStore = Stores.persistentKeyValueStore(storeName);
   

	public Topology getTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        KTable<String,Inventory> inventory = builder.stream(itemSoldTopicName, Consumed.with(Serdes.String(), itemSerde))
            // use store name as key
            .map((k,v) ->  new KeyValue<>(v.storeName, v))
            .groupByKey()
            // update the current stock for this store - item pair
            // change the value type
            .aggregate(
                () ->  new Inventory(), // initializer
                (k , newItem, aggregate) 
                    -> aggregate.updateStockQuantity(k,newItem), 
                Materialized.<String,Inventory,KeyValueStore<Bytes,byte[]>>as(storeName)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(inventorySerde));
/*
            inventory.toStream()
            .peek( (k,v) -> System.out.println(k))
            .to(inventoryStockTopicName,
                Produced.with(Serdes.String(),inventorySerde));
  */      
        return builder.build();
    }
    
    
}