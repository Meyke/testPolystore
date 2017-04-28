package test;


import java.util.HashMap;
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
		int contatore = 0;
		List<String> tabelleDaEseguirePerPrima = new LinkedList<>();
		JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
		for (int i=0; i<entitaCheConosce.size(); i++){
			String tabellaCheConosce = entitaCheConosce.get(i).getAsJsonObject().get("table").getAsString();
			String fkTabellaCheConosce = entitaCheConosce.get(i).getAsJsonObject().get("foreignkey").getAsString();
			if ((mappaWhere.get(tabellaCheConosce) != null) && (controlloFK(questoJson,fkTabellaCheConosce,mappaWhere)==true)) {  //confrontare anche se coincidono la fk di questo json con quello contenuto in mappawhere
				contatore ++;
				tabelleDaEseguirePerPrima.add(entitaCheConosce.get(i).getAsJsonObject().get("table").getAsString());
			}
		}
		if (contatore <= 1){
			System.out.println("eseguo in cascata "+ questoJson.get("table").getAsString());
			return eseguiInCascata (questoJson, risQueryPrec, jsonUtili, mappaWhere);
		}
		else {
			System.out.println("eseguo in parallelo "+ questoJson.get("table").getAsString());
			return eseguiInParallelo (questoJson, risQueryPrec, jsonUtili, mappaWhere, tabelleDaEseguirePerPrima);
		}
			
	}
	
	
	private boolean controlloFK(JsonObject questoJson, String fkTabellaCheConosce,Map<String, List<List<String>>> mappaWhere ) {
		boolean controllo = false;
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(questoJson.get("table").getAsString());
		if (condizioniPerQuellaTabella.size()!=0){
		for (List<String> condizioneIESIMA : condizioniPerQuellaTabella){
			if(condizioneIESIMA.get(0).equals(fkTabellaCheConosce)){
				controllo = true;
			}
		}
		}
		return controllo;
	}


	private JsonArray eseguiInParallelo(JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere, List<String> tabelleDaEseguirePerPrima) throws Exception {
		List<JsonArray> risultatiDaIntegrare = new LinkedList<>();
		for (String tabellaDaEseguire : tabelleDaEseguirePerPrima){
			Map<String, JsonObject> jsonUtiliModificati = new HashMap<>();
			//JsonObject tabellaInJson = jsonUtili.get(tabellaDaEseguire);
			//JsonArray risultatoParziale = esegui(tabellaInJson, null, jsonUtili,mappaWhere);
			Map<String, List<List<String>>> mappaWhereModificata = getMappaWhereModificata(questoJson,tabellaDaEseguire, mappaWhere);
			System.out.println("mappa modificata: "+ mappaWhereModificata.toString());
			jsonUtiliModificati.put(questoJson.get("table").getAsString(), questoJson);
			jsonUtiliModificati.put(tabellaDaEseguire, jsonUtili.get(tabellaDaEseguire));
			//System.out.println("risultato parziale: " + risultatoParziale.toString());
			System.out.println("jsonUtiliModificati: " + jsonUtiliModificati.toString());
			JsonArray risultato = eseguiInCascata(questoJson, risQueryPrec ,jsonUtiliModificati,mappaWhereModificata);
			System.out.println("risultato parziale :\n" + risultato.toString());
			risultatiDaIntegrare.add(risultato);
		}
		
		System.out.println("risultati da integrare: " +risultatiDaIntegrare.toString());
		String primaryKey = questoJson.get("members").getAsJsonArray().get(0).getAsString();
		JsonArray risultato = unisciRisultatiParziali(risultatiDaIntegrare, primaryKey);

		
		return risultato;
	}
	// da generalizzare se triplo join a stella (questo è solo doppio join a stella)
	private JsonArray unisciRisultatiParziali(List<JsonArray> risultatiDaIntegrare, String primaryKey) {
		JsonArray jsonArray1 = risultatiDaIntegrare.get(0);
		JsonArray jsonArray2 = risultatiDaIntegrare.get(1);
		JsonArray tuttiIRisultati = new JsonArray();
		for (int i=0; i<jsonArray1.size();i++){
			JsonObject rigaRis1 = jsonArray1.get(i).getAsJsonObject();
			String primaryKey1 = rigaRis1.get(primaryKey).getAsString(); //potevo anche mettere un campo primaryKey nel file Json. Comunque corrisponde al primo membro
			for (int j=0; j<jsonArray2.size();j++){
				JsonObject rigaRis2 = jsonArray2.get(j).getAsJsonObject();
				String primaryKey2 = rigaRis2.get(primaryKey).getAsString();
				if (primaryKey1.equals(primaryKey2)){
					tuttiIRisultati.add(rigaRis1);
				}
			}
		}
		System.out.println("risultati uniti: \n" + tuttiIRisultati.toString());
		return tuttiIRisultati;
	}


	private Map<String, List<List<String>>> getMappaWhereModificata(JsonObject questoJson, String tabellaDaEseguirePerPrima, Map<String, List<List<String>>> mappaWhere) {
		Map<String, List<List<String>>> nuovaMappaWhere = new HashMap<>();
		String questaTabella = questoJson.get("table").getAsString();
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(questaTabella);
		List<List<String>> condizioniPerTabellaDaEseguirePerPrima = mappaWhere.get(tabellaDaEseguirePerPrima);
		JsonArray tabelleCheConosce= questoJson.get("knows").getAsJsonArray();
		//mi riferisco SEMPRE A QUESTO JSON, cioè questoJson
		String foreignKeyPerTabellaPrima = null;
		for (int i=0; i<tabelleCheConosce.size(); i++){
			JsonObject tabellaIesimaCheConosce = tabelleCheConosce.get(i).getAsJsonObject();
			String tabellai = tabellaIesimaCheConosce.get("table").getAsString();
			if (tabellai.equals(tabellaDaEseguirePerPrima)){
				foreignKeyPerTabellaPrima = tabellaIesimaCheConosce.get("foreignkey").getAsString();
			}
		}
		List<List<String>> matriceWhere = new LinkedList<>(); //conterrà solo un elemento, ma per non rimodificare tutti gli altri metodi.
		for(List<String> condizioneIESIMA : condizioniPerQuellaTabella){
			if(condizioneIESIMA.get(0).equals(foreignKeyPerTabellaPrima)){
				matriceWhere.add(condizioneIESIMA);
				nuovaMappaWhere.put(questaTabella, matriceWhere);
				nuovaMappaWhere.put(tabellaDaEseguirePerPrima, condizioniPerTabellaDaEseguirePerPrima);
			}
		}
		
		return nuovaMappaWhere;
	}


	public JsonArray eseguiInCascata (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		JsonArray risultati = null;
		JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
		for (int i =0; i<entitaCheConosce.size();i++){
			JsonObject tabellaKnows = entitaCheConosce.get(i).getAsJsonObject();
			String fkTabellaCheConosce = entitaCheConosce.get(i).getAsJsonObject().get("foreignkey").getAsString();
			JsonObject altroJson = jsonUtili.get(tabellaKnows.get("table").getAsString());
			if (altroJson == null && i==entitaCheConosce.size()-1 && risultati==null)  //and mappawhere contiene la fk di questo json
				risultati = eseguiQuery(questoJson, null, mappaWhere, tabellaKnows);
			else  if((altroJson != null) && (controlloFK(questoJson,fkTabellaCheConosce,mappaWhere)==true))
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

	


