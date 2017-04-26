package postgres;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;




import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

@SuppressWarnings("deprecation")
public class PostgresServerForNeo4j {
private final static String RPC_QUEUE_NAME = "CODA_RICHIESTA_POSTGRES_DA_NEO4J";
	
	public String elaboraRisposta(String message) throws Exception{
		//il messaggio è in formato stringa e lo devo convertire in json
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject messaggioJson = parser.parse(message).getAsJsonObject();
		JsonObject questoJson = messaggioJson.get("questoJson").getAsJsonObject();
		JsonArray risQueryPrec= messaggioJson.get("risQueryPrec").getAsJsonArray();
		String mappaWhereDaRiconvertire = messaggioJson.get("mappaWhere").getAsString();
		Type listType = new TypeToken<Map<String, List<List<String>>>>() {}.getType();
		Map<String, List<List<String>>> mappaWhere = gson.fromJson(mappaWhereDaRiconvertire, listType);
		String jsonUtiliDaRiconvertire = messaggioJson.get("jsonUtili").getAsString();
		listType = new TypeToken<Map<String, JsonObject>>() {}.getType();
		Map<String, JsonObject> jsonUtili = gson.fromJson(jsonUtiliDaRiconvertire, listType);
		JsonArray risultati = new EsecutoreQuerySQL().esegui(questoJson, risQueryPrec, jsonUtili, mappaWhere);
		
		return risultati.toString();
	}

	public static void main(String[] argv) throws Exception {
		Connection connection = null;
        Channel channel;
        try {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		factory.setPort(5672);
		connection = factory.newConnection();
		channel = connection.createChannel();

		channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		
		
		QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(RPC_QUEUE_NAME, false, consumer); 
	    while (true){
	    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	    	BasicProperties props = delivery.getProperties();
	    	BasicProperties replyProps =new BasicProperties.Builder().correlationId(props.getCorrelationId()).build();
	    	String message = new String(delivery.getBody(),"UTF-8");
	    	String response = new PostgresServerForNeo4j().elaboraRisposta(message);
	    	System.out.println(" [x] Received ':'" + message + "'" + response ); 
	    	String replyToQueue = props.getReplyTo();
	    	System.out.println("Publishing to : " + replyToQueue);
	    	channel.basicPublish("", replyToQueue, replyProps, response.getBytes("UTF-8")); //meglio mettere una vera coda di risposta
	    	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	    	}
	    }
	    catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

}
