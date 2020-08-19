package ibm.gse.eda.inventory.infrastructure;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import ibm.gse.eda.inventory.domain.Item;
import io.quarkus.kafka.client.serialization.JsonbSerde;

@ApplicationScoped
public class ItemSaleAggregator {

    @Inject
    @ConfigProperty(name="mp.messaging.incoming.item-channel.topic")
    public String itemSoldTopicName;

    public String itemSoldSumTopicName = "itemSoldSum";
    public String storeName = "ItemSoldStore";

    private JsonbSerde<Item> itemSerde = new JsonbSerde<>(Item.class);
    private KeyValueBytesStoreSupplier itemSoldStore = Stores.persistentKeyValueStore(storeName);
   

	public Topology getTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(itemSoldTopicName, Consumed.with(Serdes.String(), itemSerde))
            .map((k,v) ->  new KeyValue<>((String) k,(Integer) v.quantity))
            .groupByKey(Grouped.with(Serdes.String(), Serdes.Integer()))
            .reduce(Integer::sum,Materialized.as(itemSoldStore))
            .toStream()
            .to(itemSoldSumTopicName);
        return builder.build();
	}
    
    public Item getCurrentItemStock(String itemID) {
        return itemSoldStore.get().get(itemID);
    }
}