package it.uniroma3.model;

import it.uniroma3.persistence.DocumentDao;
import it.uniroma3.persistence.GraphDao;
import it.uniroma3.persistence.RelationalDao;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.util.List;

import org.neo4j.graphdb.Result;


public class Facade {
	
	public ResultSet interrogaPostgres(String querySQL){
		RelationalDao postgres = new RelationalDao();
		ResultSet result;
		result = postgres.interroga(querySQL);
		return result;
	}
	
	public List<String> interrogaMongoDB(String queryMongoDB) throws UnknownHostException{
		DocumentDao mongo = new DocumentDao();
		List<String> documenti = mongo.interroga(queryMongoDB);
		return documenti;
		
	}
	
	public String interrogaNeo4j(String queryNeo4j){
		GraphDao neo4j = new GraphDao();
		Result result = neo4j.interroga(queryNeo4j);
		String risultati = result.resultAsString();
		neo4j.chiudiConnessione();//sempre alla fine
		return risultati;
		
		
	}

}
