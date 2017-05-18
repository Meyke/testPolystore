package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.ParserNeo4j;
import utility.CaricatoreJSON;
import utility.FabbricatoreMappaCondizioni;
import utility.GestoreRisultato;
import utility.Orchestrator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Classe Facade. E' il punto di accesso al software sottostante. Data la query iniziale, si preoccupa di instanziare
 * il parser Cypher, la mappaWhere, i jsonUtili, le tabelle e inizializzare l' orchestrator.
 * Alla fine delle varie operazioni ritorna il risultato finale della query (pattern facade)
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
		System.out.println("MAPPA CONDIZIONI tabella - condizioni su quella tabella  \n" + mappaWhere);
		List<String> tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabelle, jsonUtili, mappaWhere);
		while(tabellePriorità.size()>1){
			tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabellePriorità, jsonUtili, mappaWhere);
		}
		String tabellaPrioritàAlta = tabellePriorità.get(0);
		
		JsonObject questoJson = jsonUtili.get(tabellaPrioritàAlta);
		jsonUtili = modificaJsonUtili(jsonUtili, mappaWhere);
		System.out.println("INSIEME DI PROIEZIONI: "+parser.getListaProiezioni());
		System.out.println("JSON CONTENENTI INFORMAZIONI SULLE TABELLE DA INTERROGARE: \n "+jsonUtili.toString());
		Orchestrator gestoreQuerySql = new Orchestrator();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		risultato = GestoreRisultato.proietta(parser.getListaProiezioni(),risultato, jsonUtili);
		return risultato;
		
	}
	
	/*
	 * codice copia e incolla di facadeSQL. Ottimizzarlo spostando il metodo in una classe dedicata (quando ho tempo)
	 */
	private Map<String, JsonObject> modificaJsonUtili(Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) {
		//System.out.println(jsonUtili.toString());
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
				//System.out.println("TABELLA CHE CONOSCE: " +tabellaKnows.get("table").getAsString());
				//System.out.println("ALTROJSON: "+ (altroJson == null));
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
		//System.out.println(jsonUtiliModificati.toString());
		return jsonUtiliModificati;
	}

}
