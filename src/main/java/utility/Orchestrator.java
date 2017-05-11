package utility;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import costruttoreQuery.CostruttoreQuery;
import costruttoreQuery.CostruttoreQueryMongo;
import costruttoreQuery.CostruttoreQueryNeo4j;
import costruttoreQuery.CostruttoreQuerySQL;


/**
 * Questa classe funge da WorkflowManager data una query iniziale, interrogando le varie tabelle in cascata seguendo i collegamenti (join).
 * Puo' anche eseguire query a stella (svolto come prodotto cartesiano tra query a cascata).
 * In pratica, data la tabella con priorità più alta, da questa partiro e navighero tutti i collegamenti richiesti.
 * ES: MATCH (customer:customer), (store:store) WHERE customer.store_id = store.id AND store.address_id = 1 RETURN customer.name
 * Parto dalla tabella customer------>store. Vedo che in customer è presente un join con store, quindi eseguo store : MATCH (store:store) WHERE store.address_id = 1 RETURN store.id
 * I risultati ottenuti andranno in customer: SELECT * FROM customer WHERE  customer.store_id = i risultati della query precedente.
 * @author micheletedesco1
 *
 */
public class Orchestrator {
	//se conosce tanti ---> esegui in parallelo, altrimenti in cascata
	public JsonArray esegui (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) throws Exception{
		int contatore = 0;
		List<String> tabelleDaEseguirePerPrima = new LinkedList<>();
		JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
		for (int i=0; i<entitaCheConosce.size(); i++){
			String tabellaCheConosce = entitaCheConosce.get(i).getAsJsonObject().get("table").getAsString();
			String fkTabellaCheConosce = entitaCheConosce.get(i).getAsJsonObject().get("foreignkey").getAsString();
			if ((mappaWhere.get(tabellaCheConosce) != null) && (GestoreRisultato.controlloFK(questoJson,fkTabellaCheConosce,mappaWhere)==true)) {  //confrontare anche se coincidono la fk di questo json con quello contenuto in mappawhere
				contatore ++;
				tabelleDaEseguirePerPrima.add(entitaCheConosce.get(i).getAsJsonObject().get("table").getAsString());
			}
		}
		
		System.out.println("TABELLE DA ESEGUIRE PER PRIMA: " +tabelleDaEseguirePerPrima.toString() );
		if (contatore <= 1){
			System.out.println("eseguo in cascata "+ questoJson.get("table").getAsString());
			return eseguiInCascata (questoJson, risQueryPrec, jsonUtili, mappaWhere);
		}
		else {
			System.out.println("eseguo in parallelo "+ questoJson.get("table").getAsString());
			return eseguiInParallelo (questoJson, risQueryPrec, jsonUtili, mappaWhere, tabelleDaEseguirePerPrima);
		}
			
	}
	
	
	


	private JsonArray eseguiInParallelo(JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere, List<String> tabelleDaEseguirePerPrima) throws Exception {
		List<JsonArray> risultatiDaIntegrare = new LinkedList<>();
		for (String tabellaDaEseguire : tabelleDaEseguirePerPrima){
			Map<String, JsonObject> jsonUtiliModificati = new HashMap<>();
			//JsonObject tabellaInJson = jsonUtili.get(tabellaDaEseguire);
			//JsonArray risultatoParziale = esegui(tabellaInJson, null, jsonUtili,mappaWhere);
			Map<String, List<List<String>>> mappaWhereModificata = getMappaWhereModificata(questoJson,tabellaDaEseguire, mappaWhere, jsonUtili);
			System.out.println("mappa modificata: "+ mappaWhereModificata.toString());
			jsonUtiliModificati = getJsonUtiliModificati(jsonUtili, mappaWhereModificata);
			//jsonUtiliModificati.put(questoJson.get("table").getAsString(), questoJson);
			//jsonUtiliModificati.put(tabellaDaEseguire, jsonUtili.get(tabellaDaEseguire));
			//System.out.println("risultato parziale: " + risultatoParziale.toString());
			System.out.println("jsonUtiliModificati: " + jsonUtiliModificati.toString());
			JsonArray risultato = eseguiInCascata(questoJson, risQueryPrec ,jsonUtiliModificati,mappaWhereModificata);
			System.out.println("risultato parziale :\n" + risultato.toString());
			risultatiDaIntegrare.add(risultato);
		}
		
		System.out.println("risultati da integrare: " +risultatiDaIntegrare.toString());
		String primaryKey = questoJson.get("primarykey").getAsString();
		JsonArray risultato = unisciRisultatiParziali(risultatiDaIntegrare, primaryKey);

		
		return risultato;
	}
	/**
	 * Per gestire le query a stella. Spiegare a parole a Leo
	 * @param jsonUtili
	 * @param mappaWhereModificata
	 * @return
	 */
	private Map<String, JsonObject> getJsonUtiliModificati(Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhereModificata) {
		Map<String, JsonObject> jsonUtiliModificati = new HashMap<>();
		Set<String> tabelle = mappaWhereModificata.keySet();
		for (String s : tabelle){
			jsonUtiliModificati.put(s, jsonUtili.get(s).getAsJsonObject());
		}
		return jsonUtiliModificati;
	}


	// da generalizzare se triplo join a stella (questo è solo doppio join a stella)
	private JsonArray unisciRisultatiParziali(List<JsonArray> risultatiDaIntegrare, String primaryKey) {
		System.out.println("chiave primaria:" +primaryKey);
		JsonArray jsonArray1 = risultatiDaIntegrare.get(0);
		System.out.println("ARRAY 1: "+ jsonArray1.toString());
		JsonArray jsonArray2 = risultatiDaIntegrare.get(1);
		System.out.println("ARRAY 2: "+ jsonArray2.toString());
		JsonArray tuttiIRisultati = new JsonArray();
		for (int i=0; i<jsonArray1.size();i++){
			JsonObject rigaRis1 = jsonArray1.get(i).getAsJsonObject();
			System.out.println("RIGA "+ i + "ARRAY 1 "+ rigaRis1.toString());
			String primaryKey1 = rigaRis1.get(primaryKey).getAsString(); //potevo anche mettere un campo primaryKey nel file Json. Comunque corrisponde al primo membro
			for (int j=0; j<jsonArray2.size();j++){
				JsonObject rigaRis2 = jsonArray2.get(j).getAsJsonObject();
				System.out.println("RIGA "+ j + "ARRAY 2 "+ rigaRis2.toString());
				String primaryKey2 = rigaRis2.get(primaryKey).getAsString();
				System.out.println(primaryKey1.equals(primaryKey2));
				if (primaryKey1.equals(primaryKey2)){
					tuttiIRisultati.add(rigaRis1);
				}
			}
		}
		System.out.println("risultati uniti: \n" + tuttiIRisultati.toString());
		return tuttiIRisultati;
	}


	private Map<String, List<List<String>>> getMappaWhereModificata(JsonObject questoJson, String tabellaDaEseguirePerPrima, Map<String, List<List<String>>> mappaWhere, Map<String, JsonObject> jsonUtili) {
		Map<String, List<List<String>>> nuovaMappaWhere = new HashMap<>();
		String questaTabella = questoJson.get("table").getAsString();
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(questaTabella);
		List<List<String>> condizioniPerTabellaDaEseguirePerPrima = mappaWhere.get(tabellaDaEseguirePerPrima);
	
		  for (List<String> condizione : condizioniPerTabellaDaEseguirePerPrima){  
			  String tabella = condizione.get(1).split("\\.")[0];
		      if(mappaWhere.get(tabella)!=null)
		    	  nuovaMappaWhere.put(tabella, mappaWhere.get(tabella));
		    }
		 
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
			System.out.println("TABELLA CHE CONOSCE: " +tabellaKnows.get("table").getAsString());
			System.out.println("ALTROJSON: "+ (altroJson == null));
			if (altroJson == null && i==entitaCheConosce.size()-1 && risultati==null){ //and mappawhere contiene la fk di questo json
				System.out.println("ESEGUO A");
				risultati = eseguiQuery(questoJson, null, mappaWhere, tabellaKnows,jsonUtili);
			}
			else  if((altroJson != null) && (GestoreRisultato.controlloFK(questoJson,fkTabellaCheConosce,mappaWhere)==true)){
				System.out.println("ESEGUO B");
				risultati = eseguiQuery(questoJson, esegui(altroJson, risQueryPrec, jsonUtili, mappaWhere), mappaWhere, tabellaKnows, jsonUtili);
			
			} else if(((altroJson != null) && (GestoreRisultato.controlloFK(questoJson,fkTabellaCheConosce,mappaWhere)==false))){ //ok bidirezionali, ma non va bene con cicli
				System.out.println("ESEGUO C");
				System.out.println(risQueryPrec.toString()); //prova
				risultati = eseguiQuery(questoJson, null, mappaWhere, tabellaKnows, jsonUtili);
			}
		}
		return risultati;

	}
	
	private JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows, Map<String, JsonObject> jsonUtili) throws Exception{
		System.out.println("esegui query per: "+myJson.toString());
		CostruttoreQuery costruttoreQuery = null;
		if(myJson.get("database").getAsString().equals("postgreSQL"))
			costruttoreQuery = new CostruttoreQuerySQL();
		if(myJson.get("database").getAsString().equals("mongoDB"))
			costruttoreQuery = new CostruttoreQueryMongo();
		if(myJson.get("database").getAsString().equals("neo4j"))
			costruttoreQuery = new CostruttoreQueryNeo4j();
		return costruttoreQuery.eseguiQuery(myJson, risQueryPrec, mappaWhere,tabellaKnows, jsonUtili);
		
		
		
		}
			
	}

	


