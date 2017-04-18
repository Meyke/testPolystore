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
		//String querySQL = "SELECT * FROM indirizzo,city,country WHERE indirizzo.id_city = city.id AND city.id_country = country.id AND country.nome = 'Italia'";
		//String querySQL = "SELECT * FROM persona, scuola, indirizzo WHERE persona.id_scuola = scuola.id AND scuola.id_indirizzo = indirizzo.id AND indirizzo.nome = 'via vi'";
		//String querySQL = "SELECT * FROM city WHERE city.city = 'Roma'";
		//String querySQL = "SELECT * FROM city WHERE city.id = 1";
		//String querySQL = "SELECT * FROM city,country WHERE city.id_country = country.id AND country.nome = 'Italia'";
		
		
		
		
		
		//String querySQL = "SELECT * FROM customer";   //ok
		String querySQL = "SELECT * FROM customer, address WHERE customer.address_id = address.address_id AND address.phone = '1234'"; //ok
		
		//String querySQL = "SELECT * FROM customer, store WHERE customer.store_id = store.store_id AND store.manager_staff_id = 4"; //ok
		
		ParserSql parser = new ParserSql();
		parser.spezza(querySQL);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		System.out.println("json scaricati: " + jsonUtili);
		String tabellaPrioritàAlta = caricatoreDAFile.getTabellaPrioritaAlta();
		System.out.println("la priorità alta è della tabella : " + tabellaPrioritàAlta);
		JsonObject questoJson = jsonUtili.get(tabellaPrioritàAlta);
		FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		System.out.println("mappa di condizioni" + mappaWhere.toString());
		GestoreQuery gestoreQuerySql = new GestoreQuery();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println("risultati:");
		System.out.println(risultato.toString());

		
		
		
		/*
		String cypherQuery ="MATCH (scuola:scuola), (indirizzo:indirizzo) WHERE scuola.id_indirizzo = indirizzo.id AND indirizzo.nome = 'viale mazzini' RETURN scuola.nome";

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
		System.out.println("mappa di condizioni" + mappaWhere.toString());
		GestoreQuery gestoreQuerySql = new GestoreQuery();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println(risultato.toString());
        */

	}
}



