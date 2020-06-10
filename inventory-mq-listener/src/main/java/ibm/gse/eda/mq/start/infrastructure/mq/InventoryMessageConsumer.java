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
import javax.jms.Message;
import javax.json.bind.Jsonb;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import ibm.gse.eda.mq.start.domain.InventoryMessage;
import ibm.gse.eda.mq.start.dto.InventoryEntry;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class InventoryMessageConsumer implements Runnable {
    private static final Logger LOG = Logger.getLogger(InventoryMessageConsumer.class);
    
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
        LOG.info("@@@@ Inventory Message Consumer created @@@@");
        JmsFactoryFactory ff;
        ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
        cf = ff.createConnectionFactory();
    }

    private volatile InventoryEntry lastMessage;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("@@@@ Inventory Message Consumer on start event @@@@");
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("@@@@ Inventory Message Consumer on stop event @@@@");
        scheduler.shutdown();
    }

    public void init() throws JMSException {
        // Set the properties
        cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, this.mqHostname);
        cf.setIntProperty(WMQConstants.WMQ_PORT, this.mqHostport);
        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, this.mqQmgr);
        cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, this.appName);
        cf.setStringProperty(WMQConstants.WMQ_CHANNEL, this.mqChannel);
        cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        cf.setStringProperty(WMQConstants.USERID, this.mqAppUser);
        cf.setStringProperty(WMQConstants.PASSWORD, this.mqPassword);
        // Create JMS objects
        jmsContext = cf.createContext();
        destination = jmsContext.createQueue("queue:///" + this.mqQueueName);
        LOG.info("@@@@ " + toJson());
    }

    @Override
    public void run() {
        try {
            init();
            consumer = getJmsContext().createConsumer(getDestination()); // autoclosable
            LOG.info(("MQ JMS Consumer created entering in infinite loop"));
            while (continueToRun) {
                Message message = consumer.receive();
                if (message == null) {
                    LOG.info("receive returns `null` if the JMSConsumer is closed");
                    return;
                }
                String receivedMessage = message.getBody(String.class); // in ms or 5 seconds
                LOG.info("\n@@@@ Received message:\n" + receivedMessage);
                InventoryMessage itemSold = jsonb.fromJson(receivedMessage, InventoryMessage.class);
                // process the itemSold HERE
                lastMessage = InventoryEntry.builder(itemSold);
               
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            onStop(null);
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