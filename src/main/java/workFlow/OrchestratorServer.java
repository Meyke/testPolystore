package workFlow;

import java.io.IOException;

import workFlow.facade.FacadeCypher;
import workFlow.facade.FacadeMongo;
import workFlow.facade.FacadeSQL;

import com.google.gson.JsonArray;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;


/**
 * Questa classe riceve la query dalla Gui
 * @author micheletedesco1
 *
 */
@SuppressWarnings("deprecation")
public class OrchestratorServer {
	private Connection connection;
	private final static String RPC_QUEUE_NAME = "CODA_INVIO_QUERY_TO_ORCHESTRATOR";

	public String elaboraRisposta(String query) throws Exception{
		JsonArray risultato = new JsonArray();
		try{
			if(!query.isEmpty()){ 
				String tipoQuery = riconosciQuery(query);
				if (tipoQuery.equals("querySQL")){	
					FacadeSQL facadeSQL = new FacadeSQL();
					risultato = facadeSQL.gestisciQuery(query);
				}
				if (tipoQuery.equals("queryCypher")){
					FacadeCypher facadeCypher = new FacadeCypher();
					risultato = facadeCypher.gestisciQuery(query);
				}
				if (tipoQuery.equals("queryMongo")){
					FacadeMongo facadeCypher = new FacadeMongo();
					risultato = facadeCypher.gestisciQuery(query);
				}
				if (tipoQuery.equals("not query")){
					return "not query";
				}		
			}
		}catch (Exception e){
			e.printStackTrace();
			return "not query";
		}
		return risultato.toString();
	}





	private String riconosciQuery(String query) {
		String tipoQuery = "not query";

		if(query.startsWith("SELECT"))
			tipoQuery = "querySQL";
		if(query.startsWith("MATCH"))
			tipoQuery = "queryCypher";
		if(query.startsWith("db"))
			tipoQuery = "queryMongo";	

		return tipoQuery;
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
				String response = new OrchestratorServer().elaboraRisposta(message);
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
			if (this.connection != null) {
				try {
					this.connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void close() throws Exception {
		this.connection.close();
	}
}





