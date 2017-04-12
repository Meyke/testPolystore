package it.uniroma3.JsonUtils;


import it.uniroma3.costruttoreQuery.CostruttoreQuery;
import it.uniroma3.costruttoreQuery.CostruttoreQueryMongo;
import it.uniroma3.costruttoreQuery.CostruttoreQueryNeo4j;
import it.uniroma3.costruttoreQuery.CostruttoreQuerySQL;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Questa classe esegue la query a catena navigando sui collegamenti (sui join).
 * @author micheletedesco1
 *
 */
public class GestoreQuery {
	
	public JsonArray esegui (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonArray risultati = null;
		JsonObject altroJson = jsonUtili.get(questoJson.get("knows").getAsString());
		if (altroJson == null)
			risultati = eseguiQuery(questoJson, null, mappaWhere);
		else
			risultati = eseguiQuery(questoJson, esegui(altroJson, risQueryPrec, jsonUtili, mappaWhere), mappaWhere);
		return risultati;
				
	}
	
	private JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere) throws Exception{
		CostruttoreQuery costruttoreQuery = null;
		if(myJson.get("database").getAsString().equals("postgreSQL"))
			costruttoreQuery = new CostruttoreQuerySQL();
		if(myJson.get("database").getAsString().equals("mongoDB"))
			costruttoreQuery = new CostruttoreQueryMongo();
		if(myJson.get("database").getAsString().equals("neo4j"))
			costruttoreQuery = new CostruttoreQueryNeo4j();
		return costruttoreQuery.eseguiQuery(myJson, risQueryPrec, mappaWhere);
		
		
		
		}
			
	}

	


