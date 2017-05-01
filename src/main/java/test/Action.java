package test;


import java.util.HashSet;

//import it.uniroma3.model.QueryFacade;


import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



//per adesso la nosta action funge da WorkFlowManager
public class Action {

	public static void main(String[] args) throws Exception {
		
		//String querySQL = "SELECT * FROM customer";   //OK
		//String querySQL = "SELECT * FROM address"; //OK
		//String querySQL = "SELECT * FROM city"; //OK
		//String querySQL = "SELECT * FROM country"; //OK
		//String querySQL = "SELECT * FROM store";   //OK
	    //String querySQL = "SELECT * FROM rental"; //OK
	    //String querySQL = "SELECT * FROM payment"; //OK
	    //String querySQL = "SELECT * FROM staff"; //OK
		
		//String querySQL = "SELECT * FROM address, customer WHERE customer.address_id = address.address_id AND address.phone = '1234'"; //OK
		//String querySQL = "SELECT * FROM address, customer, city WHERE customer.address_id = address.address_id AND address.city_id = city.city_id AND city.city = 'Roma'"; //OK
		//String querySQL = "SELECT * FROM address, customer, city WHERE customer.address_id = address.address_id AND address.city_id = city.city_id"; //OK		
		//String querySQL = "SELECT * FROM customer, store WHERE customer.store_id = store.store_id AND store.manager_staff_id = 4"; //OK
		//////String querySQL = "SELECT * FROM rental, inventory WHERE rental.inventory_id = inventory.inventory_id AND inventory.inventory_id = 1"; //OK
		//String querySQL = "SELECT * FROM store, staff WHERE store.manager_staff_id = staff.staff_id AND staff.last_name = 'Giannini'"; //NOooo. Non funge con collegamenti bidirezionali (bug da risolvere)
		//String querySQL = "SELECT * FROM payment, staff WHERE payment.staff_id = staff.staff_id AND staff.last_name = 'Giannini'"; //OK
		//String querySQL = "SELECT * FROM store, address WHERE store.address_id = address.address_id AND address.address = 'via sala'";//OK
		//String querySQL = "SELECT * FROM store, staff WHERE staff.store_id = store.store_id AND store.manager_staff_id = 4"; //OK
		//String querySQL = "SELECT * FROM customer, store, address WHERE customer.store_id = store.store_id AND store.address_id = address.address_id AND address.address_id = 1"; //OK ma bug perchè customer e store conoscono entrambi address. Da risolvere se si conoscono tabelle in comune
		
		//test generatore Rsultati
//		String querySQL = "SELECT * FROM inventory, film WHERE inventory.film_id = film.film_id"; 
		String querySQL = "SELECT * FROM inventory, store WHERE inventory.store_id = store.store_id"; 
		// NON FUNZIONA!! String querySQL = "SELECT * FROM rental, inventory, customer WHERE rental.customer_id = customer.customer_id AND rental.inventory_id = inventory.inventory_id AND customer.last_name = 'Rossi'";
		
		
		ParserSql parser = new ParserSql();
		parser.spezza(querySQL);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		System.out.println("MATRICE WHERE = "+ matriceWhere.toString() +"\n");
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		System.out.println("JSON SCARICATI : " + jsonUtili+"\n");
		List<String> tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabelle, jsonUtili);
		while(tabellePriorità.size()>1){
			tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabellePriorità, jsonUtili);
		}
		String tabellaPrioritàAlta = tabellePriorità.get(0);
		System.out.println("la priorità alta è della tabella : " + tabellaPrioritàAlta+"\n");
		JsonObject questoJson = jsonUtili.get(tabellaPrioritàAlta);
		FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		System.out.println("MAPPA WHERE :" + mappaWhere.toString()+"\n");
		GestoreQuery gestoreQuerySql = new GestoreQuery();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		System.out.println("\nRISULTATI FINALI:\n");
		System.out.println(risultato.toString()+"\n");
	
//--ricordarsi che il server di mongoDB deve sempre essere acceso, altrimenti non funziona.
		
		
		
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
		
//		if(a.get(attrCorrente) == null)
//			attrCorrente = attributiSecondoMembro.get(j).getAsString().split("\\.")[1];
//		if(b.get(attrCorrente.split("\\.")[1])==null && a.get(attrCorrente) != null)
//			b.add(attrCorrente.split("\\.")[1], a.get(attrCorrente));
//		else if(b.get(attrCorrente.split("\\.")[1])==null)
//			b.add(attrCorrente.split("\\.")[1], a.get(attrCorrente.split("\\.")[1]));
//	}
	}
}


