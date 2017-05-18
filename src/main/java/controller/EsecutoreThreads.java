package controller;

import mongo.MongoServer;
import neo4j.Neo4jServer;
import postgres.PostgresServer;
import workFlow.OrchestratorServer;

public class EsecutoreThreads {
	
	private Thread thread1;
	private Thread thread2;
	private Thread thread3;
	private Thread thread4;
	private OrchestratorServer orchestratorServer;
	private PostgresServer postgresServer;
	private Neo4jServer neo4jServer;
	private MongoServer mongoServer;
	
	
	
	public EsecutoreThreads() {
		this.orchestratorServer = new OrchestratorServer();
		this.postgresServer = new PostgresServer();
		this.neo4jServer = new Neo4jServer();
		this.mongoServer = new MongoServer();
	}


	//potevo usare anche ExecutorService
	public void avviaServer() throws Exception{
		
		this.thread1 = new Thread() {
		    public void run() {
		    	try {
					orchestratorServer.avvia();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		};

		this.thread2 = new Thread() {
		    public void run() {
		    	try {
					postgresServer.avvia();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		};
		
		this.thread3 = new Thread() {
		    public void run() {
		    	try {
		    		neo4jServer.avvia();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		};
		
		this.thread4 = new Thread() {
		    public void run() {
		    	try {
		    		mongoServer.avvia();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		};
		
		this.thread1.start();
		this.thread2.start();
		this.thread3.start();
		this.thread4.start();
		System.out.println("prova");
			
	}
	
	
	public static void main(String[] args) throws Exception{
		EsecutoreThreads esecutore = new EsecutoreThreads();
		esecutore.avviaServer();
		Thread.sleep(2000);
		//esecutore.stopServer();
		
		
	}


	public void stopServer() throws Exception {
		this.orchestratorServer.close();
		this.neo4jServer.close();
		this.mongoServer.close();
		this.postgresServer.close();
		
	}









	

}
