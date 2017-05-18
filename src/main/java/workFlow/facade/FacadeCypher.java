package workFlow.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.ParserNeo4j;
import utility.CaricatoreJSON;
import utility.FabbricatoreMappaCondizioni;
import utility.GestoreRisultato;
import workFlow.Orchestrator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Data la query Cypher iniziale, interroga tutte le tabelle e restituisce il risultato
 * @author micheletedesco1
 *
 */

/*
 * Questa classe funge da WorkflowManager data una query Cypher iniziale, interrogando le varie tabelle in cascata seguendo i collegamenti (join).
 * In pratica, data la tabella con priorità più alta, da questa partiro e navighero tutti i collegamenti richiesti (si veda la classe GestoreQuery).
 * ES: MATCH (customer:customer), (store:store) WHERE customer.store_id = store.id AND store.address_id = 1 RETURN customer.name
 * Parto dalla tabella customer------>store. Vedo che in customer è presente un join con store, quindi eseguo store : MATCH (store:store) WHERE store.address_id = 1 RETURN store.id
 * I risultati ottenuti andranno in customer: SELECT * FROM customer WHERE  customer.store_id = i risultati della query precedente.
 * @author micheletedesco1
 *
 */
public class FacadeCypher {
	public JsonArray gestisciQuery(String queryCypher) throws Exception{
		ParserNeo4j parser = new ParserNeo4j();
		parser.spezza(queryCypher);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		FabbricatoreMappaCondizioni fabbricatoreCondizione = new FabbricatoreMappaCondizioni();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		
		List<String> tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabelle, jsonUtili, mappaWhere);
		while(tabellePriorità.size()>1){
			tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabellePriorità, jsonUtili, mappaWhere);
		}
		String tabellaPrioritàAlta = tabellePriorità.get(0);
		
		JsonObject questoJson = jsonUtili.get(tabellaPrioritàAlta);
		jsonUtili = modificaJsonUtili(jsonUtili, mappaWhere);
		Orchestrator gestoreQuery = new Orchestrator();
		JsonArray risultato = gestoreQuery.esegui(questoJson, new JsonArray(), jsonUtili, mappaWhere);
		System.out.println("lista proiezioni: "+parser.getListaProiezioni());
		System.out.println("jsonUtili: "+jsonUtili.toString());
		System.out.println("mappaWhere: "+mappaWhere.toString());
		System.out.println("RISULTATI: "+ risultato.toString());
		risultato = GestoreRisultato.proietta(parser.getListaProiezioni(),risultato, jsonUtili);
		gestoreQuery.close();
		return risultato;
		
	}
	
	private Map<String, JsonObject> modificaJsonUtili(Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) {
		System.out.println(jsonUtili.toString());
		Map<String, JsonObject> jsonUtiliModificati = new HashMap<>();
		Set<String> tabelle = jsonUtili.keySet();
		for (String t : tabelle){
			JsonObject questoJson = jsonUtili.get(t).getAsJsonObject();
			JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
			JsonArray entitaCheConosceModificate = new JsonArray();
			for (int i =0; i<entitaCheConosce.size();i++){
				JsonObject tabellaKnows = entitaCheConosce.get(i).getAsJsonObject();
				String fkTabellaCheConosce = entitaCheConosce.get(i).getAsJsonObject().get("foreignkey").getAsString();
				JsonObject altroJson = jsonUtili.get(tabellaKnows.get("table").getAsString());
				System.out.println("TABELLA CHE CONOSCE: " +tabellaKnows.get("table").getAsString());
				System.out.println("ALTROJSON: "+ (altroJson == null));
				if(((altroJson != null) && (GestoreRisultato.controlloFK(questoJson,fkTabellaCheConosce,mappaWhere)==true))){
					entitaCheConosceModificate.add(tabellaKnows);
				}
				else {
					tabellaKnows.addProperty("table", "nessuno");
					tabellaKnows.addProperty("foreignkey", "nessuna");
					entitaCheConosceModificate.add(tabellaKnows);
				}
			}
			questoJson.add("knows", entitaCheConosceModificate);
			jsonUtiliModificati.put(t, questoJson);		
		}
		System.out.println(jsonUtiliModificati.toString());
		return jsonUtiliModificati;
	}

}
