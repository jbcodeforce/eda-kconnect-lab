package ibm.gse.eda.mq.start.infrastructure.mq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.json.bind.Jsonb;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import ibm.gse.eda.mq.start.domain.InventoryMessage;
import ibm.gse.eda.mq.start.dto.InventoryEntry;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class InventoryMessageConsumer implements Runnable {

    @Inject
    @ConfigProperty(name = "mq.host")
    public String mqHostname;

    @Inject
    @ConfigProperty(name = "mq.port")
    public int mqHostport;

    @Inject
    @ConfigProperty(name = "mq.qmgr", defaultValue = "QM1")
    public String mqQmgr;

    @Inject
    @ConfigProperty(name = "mq.channel", defaultValue = "DEV.APP.SVRCONN")
    public String mqChannel;

    @Inject
    @ConfigProperty(name = "mq.app_user", defaultValue = "app")
    public String mqAppUser;

    @Inject
    @ConfigProperty(name = "mq.app_password", defaultValue = "passw0rd")
    public String mqPassword;

    @Inject
    @ConfigProperty(name = "mq.queue_name", defaultValue = "DEV.QUEUE.1")
    public String mqQueueName;

    @Inject
    @ConfigProperty(name = "app.name", defaultValue = "TestApp")
    public String appName;


    protected JMSConsumer consumer = null;
    private JMSContext jmsContext = null;
    private Destination destination = null;
    private JmsConnectionFactory cf = null;
    protected Jsonb jsonb = null;
    protected boolean continueToRun = true;
    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();
    
    public InventoryMessageConsumer() throws JMSException {
        JmsFactoryFactory ff;
        ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
        cf = ff.createConnectionFactory();
    }

    private volatile InventoryEntry lastMessage;

    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        consumer = getJmsContext().createConsumer(getDestination()); // autoclosable
        while (continueToRun) {
            String receivedMessage = consumer.receiveBody(String.class, 15000); // in ms or 15 seconds
            if (receivedMessage == null) {
                // receive returns `null` if the JMSConsumer is closed
                return;
            }
            InventoryMessage itemSold = jsonb.fromJson(receivedMessage, InventoryMessage.class);
            // process the itemSold HERE
            lastMessage = InventoryEntry.builder(itemSold);
            System.out.println("\nReceived message:\n" + receivedMessage);
        }
    }

    public JMSContext getJmsContext() {
        return jmsContext;
    }

    public Destination getDestination() {
        return destination;
    }

    public String toJson(){
        return "{ \"host\": \"" + mqHostname
               + "\", \"port\": " + mqHostport 
               + ", \"qmgr\": \"" + mqQmgr 
               + "\", \"mqChannel\": \"" + mqChannel 
               + "\", \"mqQueueName\": \"" + mqQueueName 
               + "\", \"mqAppUser\": \"" + mqAppUser 
               + "\"}";
    }

	public InventoryEntry getLastMessage() {
		return lastMessage;
	}
}