package ibm.gse.eda.stores.infrastructure;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import ibm.gse.eda.stores.domain.ItemSaleMessage;
import io.reactivex.Flowable;

@ApplicationScoped
public class ItemSaleGenerator {
    private static Logger LOG = Logger.getLogger(ItemSaleGenerator.class.getName());
 
    
    @Inject
    @ConfigProperty(name = "amqp.queue")
    public String queueName;
    @Inject
    @ConfigProperty(name = "amqp.host")
    public String hostname;
    @Inject
    @ConfigProperty(name = "amqp.port")
    public int port;

    @Inject
    @ConfigProperty(name = "amqp.username")
    public String username;
    @Inject
    @ConfigProperty(name = "amqp.password")
    public String password;
    @Inject 
    @ConfigProperty(name = "amqp.virtualHost")
    public String virtualHost;


    private long id = 0;
    private String[] stores = {"SC01","SF01","SF02","PT01","PT02","SEA01","NYC01","NYC02","LA01","LA02"};
    private Random random = new Random();
    private ItemSaleMessage lastItem;
    private Channel channel;
    private Connection connection;
    private ConnectionFactory factory;

    private ItemSaleMessage buildNext(){
        ItemSaleMessage item = new ItemSaleMessage();
        item.id = id;
        item.storeName=stores[random.nextInt(stores.length)];
        item.sku = "IT0" + random.nextInt(9);
        item.quantity = random.nextInt(9);
        item.price = random.nextDouble() * 70;
        id++;
        lastItem = item;
        return item;
    } 
    

    public Flowable<ItemSaleMessage> generateItemSale() {
        return Flowable.interval(5,TimeUnit.SECONDS).map(tick -> buildNext());
    }

    public ItemSaleMessage getLastItem(){
        return this.lastItem;
    }


	public void start(int records) {
        this.factory = new ConnectionFactory();
        this.factory.setHost(hostname);
        this.factory.setPort(port);
        this.factory.setUsername(username);
        this.factory.setPassword(password);
        this.factory.setVirtualHost(virtualHost);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            Jsonb parser = JsonbBuilder.create();
            for (int i = 0; i < records; i++) {
                ItemSaleMessage item = buildNext();
                String messageToSend = parser.toJson(item);
                LOG.info(messageToSend);
                this.channel.basicPublish("", queueName, null, messageToSend.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
       
	}
}