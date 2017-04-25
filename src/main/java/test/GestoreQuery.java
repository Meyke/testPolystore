package test;


import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * questo è il mio Orchestrator
 * @author micheletedesco1
 *
 */
public class GestoreQuery {
	
	public JsonArray esegui (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonArray risultati = null;
		JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
		System.out.println("entita che conosce " + entitaCheConosce.size());
		for (int i =0; i<entitaCheConosce.size();i++){
			JsonObject tabellaKnows = entitaCheConosce.get(i).getAsJsonObject();
			System.out.println("tabellaKnows: " + tabellaKnows.toString());
			JsonObject altroJson = jsonUtili.get(tabellaKnows.get("table").getAsString());
			if (altroJson == null && i==entitaCheConosce.size()-1 && risultati==null) 
				risultati = eseguiQuery(questoJson, null, mappaWhere, tabellaKnows);
			else  if(altroJson != null)
				risultati = eseguiQuery(questoJson, esegui(altroJson, risQueryPrec, jsonUtili, mappaWhere), mappaWhere, tabellaKnows);
		}
		return risultati;

	}
	
	private JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows) throws Exception{
		CostruttoreQuery costruttoreQuery = null;
		if(myJson.get("database").getAsString().equals("postgreSQL"))
			costruttoreQuery = new CostruttoreQuerySQL();
		if(myJson.get("database").getAsString().equals("mongoDB"))
			costruttoreQuery = new CostruttoreQueryMongo();
		if(myJson.get("database").getAsString().equals("neo4j"))
			costruttoreQuery = new CostruttoreQueryNeo4j();
		return costruttoreQuery.eseguiQuery(myJson, risQueryPrec, mappaWhere,tabellaKnows);
		
		
		
		}
			
	}

	


