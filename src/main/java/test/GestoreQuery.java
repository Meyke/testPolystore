package test;


import java.util.LinkedList;
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
	//se conosce tanti ---> esegui in parallelo, altrimenti in cascata
	public JsonArray esegui (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		int contatore = 1;
		List<String> tabelleDaEseguirePerPrima = new LinkedList<>();
		JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
		for (int i=0; i<entitaCheConosce.size(); i++){
			if (mappaWhere.get(entitaCheConosce.get(i).getAsJsonObject().get("table").getAsString()) != null) {  //confrontare anche se coincidono la fk di questo json con quello contenuto in mappawhere
				contatore ++;
				tabelleDaEseguirePerPrima.add(entitaCheConosce.get(i).getAsJsonObject().get("table").getAsString());
			}
		}
		if (contatore == 1)
			return eseguiInCascata (questoJson, risQueryPrec, jsonUtili, mappaWhere);
		else 
			return eseguiInParallelo (questoJson, risQueryPrec, jsonUtili, mappaWhere, tabelleDaEseguirePerPrima);
	}
	
	
	private JsonArray eseguiInParallelo(JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere, List<String> tabelleDaEseguirePerPrima) throws Exception {
		//da fare
		for (String tabellaDaEseguire : tabelleDaEseguirePerPrima){
			JsonObject tabellaInJson = jsonUtili.get(tabellaDaEseguire);
			jsonUtili.remove(tabellaDaEseguire);
			JsonArray risultatoParziale = esegui(tabellaInJson, risQueryPrec, jsonUtili,mappaWhere);
			System.out.println(risultatoParziale.toString());
			//aggiungo i risultati parziali in una lista
			//....da finire. Da creare nei costruttoriQuery un metodo che effettui join paralleli
		}
		
//infine eseguo in cascata la prima tabella padre (cioè questoJson) mettendo i risQueryPrec in modo buono
		
		return null;
	}
	public JsonArray eseguiInCascata (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonArray risultati = null;
		jsonUtili.remove(questoJson.get("table").getAsString());//per evitare cicli di tabelle
		JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
		for (int i =0; i<entitaCheConosce.size();i++){
			JsonObject tabellaKnows = entitaCheConosce.get(i).getAsJsonObject();
			JsonObject altroJson = jsonUtili.get(tabellaKnows.get("table").getAsString());
			if (altroJson == null && i==entitaCheConosce.size()-1 && risultati==null)  //and mappawhere contiene la fk di questo json
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

	


