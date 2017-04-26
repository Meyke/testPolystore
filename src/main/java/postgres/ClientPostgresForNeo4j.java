package postgres;

import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * client del docker postgres che chiede al docker neo4j di eseguire una query per integrarla nel suo risultato
 * @author micheletedesco1
 *
 */
@SuppressWarnings("deprecation")
public class ClientPostgresForNeo4j {
	private Connection connection;
    private Channel channel;
    private String requestQueueName;
    private String replyQueueName = "CODA_RICEZIONE_POSTGRES_DA_NEO4J";
	private QueueingConsumer consumer;
	
	
	public ClientPostgresForNeo4j() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();

		replyQueueName = channel.queueDeclare(replyQueueName, false, false, false, null).getQueue();
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
	}


	public JsonArray callNeo4j(JsonObject messaggioJson) throws UnsupportedEncodingException, IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException{
		String response = null;
		String corrId = java.util.UUID.randomUUID().toString();
	    AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();
	    String message = messaggioJson.toString();
	    this.requestQueueName = "CODA_RICHIESTA_NEO4J_DA_POSTGRES" ;
		channel.basicPublish("", this.requestQueueName, props, message.getBytes("UTF-8"));
		
		//gestione del ritorno del risultato delle query
		while (true) {
	        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
	            response = new String(delivery.getBody());
	            break;
	        }
	    }
		//la risposta sarà un jsonArray in formato stringa. Lo devo riconvertire
		JsonParser parser = new JsonParser();
		JsonArray risultati = parser.parse(response).getAsJsonArray();
	    return risultati;
		
		
	}
	
	

}
