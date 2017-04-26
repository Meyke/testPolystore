package postgres;

import java.util.Map;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class EsecutoreQuerySQL {
	public JsonArray esegui (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonArray risultati = new JsonArray();
		JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
		System.out.println("entita che conosce " + entitaCheConosce.size());
		for (int i =0; i<entitaCheConosce.size();i++){
			JsonObject tabellaKnows = entitaCheConosce.get(i).getAsJsonObject();
			System.out.println("tabellaKnows: " + tabellaKnows.toString());
			JsonObject altroJson = jsonUtili.get(tabellaKnows.get("table").getAsString());
			if ( altroJson == null && i==entitaCheConosce.size()-1 && risultati.size()==0 ) 
				risultati = eseguiQuery(questoJson, null, mappaWhere, tabellaKnows);
			else  if(altroJson != null)
				risultati = eseguiQuery(questoJson, eseguiAltraQuery(altroJson, risQueryPrec, jsonUtili, mappaWhere), mappaWhere, tabellaKnows);
		}
		return risultati;

	}
	
	private JsonArray eseguiQuery(JsonObject myJson, JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows) throws Exception {
		CostruttoreQuerySQL costruttoreQuery = new CostruttoreQuerySQL();
		return costruttoreQuery.eseguiQuery(myJson, risQueryPrec, mappaWhere,tabellaKnows);
	}

	private JsonArray eseguiAltraQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonArray risultati = null;
		if(myJson.get("database").getAsString().equals("postgreSQL")){
			//rimango nel mio docker
			EsecutoreQuerySQL esecutoreQuery = new EsecutoreQuerySQL();
			risultati = esecutoreQuery.esegui(myJson, risQueryPrec, jsonUtili, mappaWhere);
			
		}
		if(myJson.get("database").getAsString().equals("neo4j")){
			//chiedo i risultati al docker neo4j
			JsonObject messaggioJson = creaJson(myJson, risQueryPrec, jsonUtili, mappaWhere);
			risultati = new ClientPostgresForNeo4j().callNeo4j(messaggioJson);
			//ricordarsi di abbattere il clientPostgresForNeo4j. Altrimenti poi avrei diversi client. Per ora ok
		}
		if(myJson.get("database").getAsString().equals("mongoDB")){
			//invio al docker mongoDB
		}
		return risultati;
		
		
		
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
			
}


