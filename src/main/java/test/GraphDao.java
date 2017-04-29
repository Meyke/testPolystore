package test;

import java.io.File;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class GraphDao {
	private GraphDatabaseFactory dbFactory;
	private GraphDatabaseService graphDB;
	
	public GraphDao(){
		this.dbFactory = new GraphDatabaseFactory();
		File storeFile = new File("/Users/leorossi/Documents/Neo4j/default.graphdb");
		this.graphDB = dbFactory.newEmbeddedDatabase(storeFile);
	}
	
	public Result interroga(String queryCQL){
		Result result = graphDB.execute(queryCQL);
        return result;
	}
	
	public void chiudiConnessione(){
		this.graphDB.shutdown();
	}
	

}
