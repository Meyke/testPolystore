package test;


//import it.uniroma3.model.QueryFacade;


import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



//per adesso la nosta action funge da WorkFlowManager
public class Action {

	public static void main(String[] args) throws Exception {
		/*
		String querySQL = "SELECT * FROM persona, scuola WHERE persona.id_scuola = scuola.id AND scuola.nome = 'caffe'";
		
		ParserSql parser = new ParserSql();
		parser.spezza(querySQL);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		JsonObject questoJson = jsonUtili.get("persona");
		FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		System.out.println(mappaWhere.toString());
		GestoreQuery gestoreQuerySql = new GestoreQuery();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println(risultato.toString());
		System.out.println("------------------");
		System.out.println("------------------");
		*/
		
		//-------altro test
		
		String querySQL = "SELECT * FROM scuola, indirizzo WHERE scuola.id_indirizzo = indirizzo.id AND indirizzo.nome = 'brava'";
		 

		ParserSql parser = new ParserSql();
		parser.spezza(querySQL);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		JsonObject questoJson = jsonUtili.get("scuola");
		FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		System.out.println(mappaWhere.toString());
		GestoreQuery gestoreQuerySql = new GestoreQuery();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println(risultato.toString());

		





	}
}



