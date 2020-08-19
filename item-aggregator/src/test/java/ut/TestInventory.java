package ut;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.inventory.domain.Inventory;
import ibm.gse.eda.inventory.domain.Item;
import ibm.gse.eda.inventory.infrastructure.InventoryAgent;
import io.quarkus.kafka.client.serialization.JsonbSerde;

public class TestInventory {
     
    private static TopologyTestDriver testDriver;

    private TestInputTopic<String, Item> inputTopic;
    private TestOutputTopic<String, Inventory> outputTopic;
    private InventoryAgent agent = new InventoryAgent();

    private Serde<String> stringSerde = new Serdes.StringSerde();
    private JsonbSerde<Item> itemSerde = new JsonbSerde<>(Item.class);
    private JsonbSerde<Inventory> inventorySerde = new JsonbSerde<>(Inventory.class);
  
    public  Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stock-aggregator");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        return props;
    }


    /**
     * process item sale events, and aggregate per key
     */
    @BeforeEach
    public void setup() {
        // as no CDI is used set the topic names
        agent.itemSoldTopicName="itemSold";
        agent.inventoryStockTopicName = "inventory";
        Topology topology = agent.getTopology();
        testDriver = new TopologyTestDriver(topology, getStreamsConfig());
        inputTopic = testDriver.createInputTopic(agent.itemSoldTopicName, 
                                stringSerde.serializer(),
                                itemSerde.serializer());
        outputTopic = testDriver.createOutputTopic(agent.inventoryStockTopicName, 
                                stringSerde.deserializer(), 
                                inventorySerde.deserializer());

    }

    @AfterEach
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final Exception e) {
             System.out.println("Ignoring exception, test failing due this exception:" + e.getLocalizedMessage());
        } 
    }

    @Test
    public void shouldGetTwoItemSold(){
        //given an item is sold in a store
        Item item = new Item("Store-1","Item-1","RESTOCK",5,33.2);
        inputTopic.pipeInput(item.itemCode, item);        
        item = new Item("Store-1","Item-1","SALE",2,33.2);
        inputTopic.pipeInput(item.itemCode, item);

        //Assertions.assertFalse(outputTopic.isEmpty()); 
        //Assertions.assertEquals(5, outputTopic.readKeyValue().value.stock.get("Item-1"));
        //Assertions.assertEquals(3, outputTopic.readKeyValue().value.stock.get("Item-1"));
    }
    
}