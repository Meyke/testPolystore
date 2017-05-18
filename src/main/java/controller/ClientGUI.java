package controller;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
/**
 * Client pattern RPC di rabbitMQ. Ricevuta la query dalla form di input, invia una richiesta di esecuzione
 * all'Orchestrator. Quindi invia il messaggio in una queue in cui è in ascolto il WorkflowManager
 * @author micheletedesco1
 *
 */
@SuppressWarnings("deprecation")
public class ClientGUI {
	private Connection connection;
	private Channel channel;
	private String replyQueueName;
	private QueueingConsumer consumer;
	private final static String RPC_QUEUE_NAME = "CODA_INVIO_QUERY_TO_ORCHESTRATOR";
	
	public ClientGUI() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		this.connection = factory.newConnection();
		this.channel = connection.createChannel();

		replyQueueName = channel.queueDeclare().getQueue();
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
	}

	public String call(String query) throws IOException, InterruptedException{
		String response = null;
	    String corrId = java.util.UUID.randomUUID().toString();
	    AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build(); 
		channel.basicPublish("", RPC_QUEUE_NAME, props, query.getBytes("UTF-8"));
		
		
		
		//gestione del ritorno del risultato delle query
		while (true) {
	        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
	            response = new String(delivery.getBody());
	            break;
	        }
	    }
	    return response;
	}
	
	public void close() throws Exception {
	    connection.close();
	}

}
