package test;


//import it.uniroma3.model.QueryFacade;


import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



//per adesso la nosta action funge da WorkFlowManager
public class Action {

	public static void main(String[] args) throws Exception {
		
		
		
		
		//-------altro test
		//String querySQL = "SELECT * FROM persona, scuola WHERE persona.id_scuola = scuola.id AND scuola.nome = 'caffe'";
		//String querySQL = "SELECT * FROM scuola, indirizzo WHERE scuola.id_indirizzo = indirizzo.id AND indirizzo.nome = 'brava'";
		//String querySQL = "SELECT * FROM indirizzo";
		/*
		String querySQL = "SELECT * FROM persona, scuola, indirizzo WHERE persona.id_scuola = scuola.id AND scuola.id_indirizzo = indirizzo.id AND indirizzo.nome = 'brava'";

		ParserSql parser = new ParserSql();
		parser.spezza(querySQL);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		String tabellaPrioritàAlta = caricatoreDAFile.getTabellaPrioritaAlta();
		System.out.println("la priorità alta è della tabella : " + tabellaPrioritàAlta);
		JsonObject questoJson = jsonUtili.get(tabellaPrioritàAlta);
		FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		System.out.println(mappaWhere.toString());
		GestoreQuery gestoreQuerySql = new GestoreQuery();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println(risultato.toString());

		*/
		
		
		
		//String cypherQuery ="MATCH (scuola:scuola), (indirizzo:indirizzo) WHERE persona.id_scuola = scuola.id AND scuola.id_indirizzo = indirizzo.id AND indirizzo.nome = 'brava' RETURN scuola.nome";
        String cypherQuery ="MATCH (persona:persona), (scuola:scuola), (indirizzo:indirizzo) WHERE scuola.id_indirizzo = indirizzo.id AND indirizzo.nome = 'brava' RETURN scuola.nome";
		ParserNeo4j parser = new ParserNeo4j();
		parser.spezza(cypherQuery);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		String tabellaPrioritàAlta = caricatoreDAFile.getTabellaPrioritaAlta();
		System.out.println("la priorità alta è della tabella : " + tabellaPrioritàAlta);
		JsonObject questoJson = jsonUtili.get(tabellaPrioritàAlta);
		FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		System.out.println(mappaWhere.toString());
		GestoreQuery gestoreQuery = new GestoreQuery();
		JsonArray risultato = gestoreQuery.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println(risultato.toString());
        

	}
}



