package mongo;

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
public class MongoServer {
	private Connection connection;
	private final static String RPC_QUEUE_NAME = "CODA_QUERY_TO_MONGO";
	private String CODA_RISPOSTA;	
	
	public String elaboraRisposta(String message) throws Exception{
		//il messaggio è in formato stringa e lo devo convertire in json
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject messaggioJson = parser.parse(message).getAsJsonObject();
		JsonObject questoJson = messaggioJson.get("questoJson").getAsJsonObject();
		JsonArray risQueryPrec= messaggioJson.get("risQueryPrec").getAsJsonArray();
		String codaRisposta = messaggioJson.get("codaRisposta").getAsString();
		System.out.println("coda risposta: " + codaRisposta);
		String mappaWhereDaRiconvertire = messaggioJson.get("mappaWhere").getAsString();
		Type listType = new TypeToken<Map<String, List<List<String>>>>() {}.getType();
		Map<String, List<List<String>>> mappaWhere = gson.fromJson(mappaWhereDaRiconvertire, listType);
		String jsonUtiliDaRiconvertire = messaggioJson.get("jsonUtili").getAsString();
		listType = new TypeToken<Map<String, JsonObject>>() {}.getType();
		Map<String, JsonObject> jsonUtili = gson.fromJson(jsonUtiliDaRiconvertire, listType);				
		JsonArray risultati = new EsecutoreQueryMONGO().esegui(questoJson, risQueryPrec, jsonUtili, mappaWhere);

		this.CODA_RISPOSTA = codaRisposta;
		return risultati.toString();
	}

	public void avvia() throws Exception {
		this.connection = null;
		Channel channel;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			factory.setPort(5672);
			this.connection = factory.newConnection();
			channel = this.connection.createChannel();

			channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");


			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(RPC_QUEUE_NAME, false, consumer); 
			while (true){
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps =new BasicProperties.Builder().correlationId(props.getCorrelationId()).build();
				String message = new String(delivery.getBody(),"UTF-8");
				System.out.println("MESSAGGIO: "+message);
				MongoServer mongoServer = new MongoServer();
				String response = mongoServer.elaboraRisposta(message);
				System.out.println(" [x] Received ':'" + message + "'" + response ); 

				String replyToQueue = mongoServer.getCODA_RISPOSTA();
				System.out.println("Publishing to : " + replyToQueue);
				channel.basicPublish("", replyToQueue, replyProps, response.getBytes("UTF-8")); 
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (this.connection != null) {
				try {
					this.connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getCODA_RISPOSTA() {
		return CODA_RISPOSTA;
	}
	
	public void close() throws Exception {
	    this.connection.close();
	}

}
