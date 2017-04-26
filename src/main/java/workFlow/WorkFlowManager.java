package workFlow;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;


/**
 * Questo è il mio Orchestrator
 * @author micheletedesco1
 *
 */
@SuppressWarnings("deprecation")
public class WorkFlowManager {
	private Connection connection;
    private Channel channel;
    private String requestQueueName;
    private String replyQueueName = "CODA_RICEZIONE_WORKFLOW";
	private QueueingConsumer consumer;

	public WorkFlowManager() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();

		replyQueueName = channel.queueDeclare(replyQueueName, false, false, false, null).getQueue();
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
	}

	public JsonArray esegui (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonArray risultati = null;
		risultati = eseguiQuery(questoJson, risQueryPrec, jsonUtili, mappaWhere);
		return risultati;

	}

	private JsonArray eseguiQuery(JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonObject messaggioJson = creaJson(questoJson, risQueryPrec, jsonUtili, mappaWhere);;
		JsonArray risultato = null;
		String database = questoJson.get("database").getAsString();;
		risultato = callDatabase(messaggioJson, database);
		return risultato;



	}

	private JsonObject creaJson(JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) {
		JsonObject messaggioJson = new JsonObject();
		messaggioJson.add("questoJson", questoJson);
		messaggioJson.add("risQueryPrec", risQueryPrec);
		Gson gson = new Gson();
		String mappaWhereDaRiconvertire = gson.toJson(mappaWhere);
		String jsonUtiliDaRiconvertire = gson.toJson(jsonUtili);
		messaggioJson.addProperty("jsonUtili", jsonUtiliDaRiconvertire);
		messaggioJson.addProperty("mappaWhere", mappaWhereDaRiconvertire);
		return messaggioJson;

	}
	//uso del pattern RPC
	private JsonArray callDatabase(JsonObject messaggioJson, String database) throws UnsupportedEncodingException, IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		String response = null;
		String corrId = java.util.UUID.randomUUID().toString();
	    AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();
	    String message = messaggioJson.toString();
	    if (database.equals("postgreSQL"))
	    	this.requestQueueName = "CODA_RICHIESTA_POSTGRES_DA_WF" ;
	    if (database.equals("mongoDB"))
	    	this.requestQueueName = "CODA_RICHIESTA_MONGO_DA_WF" ;
	    if (database.equals("neo4j"))
	    	this.requestQueueName = "CODA_RICHIESTA_NEO4J_DA_WF" ;
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




