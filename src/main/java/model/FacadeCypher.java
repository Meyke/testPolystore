package model;

import java.util.List;
import java.util.Map;

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
		
		List<String> tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabelle, jsonUtili, mappaWhere);
		while(tabellePriorità.size()>1){
			tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabellePriorità, jsonUtili, mappaWhere);
		}
		String tabellaPrioritàAlta = tabellePriorità.get(0);
		
		JsonObject questoJson = jsonUtili.get(tabellaPrioritàAlta);
		Orchestrator gestoreQuerySql = new Orchestrator();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println("lista proiezioni: "+parser.getListaProiezioni());
		System.out.println("jsonUtili: "+jsonUtili.toString());
		System.out.println("mappaWhere: "+mappaWhere.toString());
		System.out.println("RISULTATI: "+ risultato.toString());
		risultato = GestoreRisultato.proietta(parser.getListaProiezioni(),risultato, jsonUtili);
		return risultato;
		
	}

}
