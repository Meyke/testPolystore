package daSistemare;

import java.util.List;
import java.util.Map;





import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Action2 {
public static void main(String[] args) throws Exception {
		
//--ricordarsi che il server di mongoDB deve sempre essere acceso, altrimenti non funziona.
	        
	        //String queryCypher = "MATCH (customer:customer) RETURN customer";   //OK
			//String queryCypher = "MATCH (address:address) RETURN address"; //OK
	        //String queryCypher = "MATCH (city:city) RETURN city"; //OK
	        //String queryCypher = "MATCH (country:country) RETURN country";//OK
	        //String queryCypher = "MATCH (store:store) RETURN store"; //OK
	        //String queryCypher = "MATCH (rental:rental) RETURN rental"; //OK
	        //String queryCypher = "MATCH (payment:payment) RETURN payment"; //OK
	        String queryCypher = "MATCH (staff:staff) RETURN staff"; //OK
	        //String queryCypher ="MATCH (address:address), (customer:customer) WHERE customer.address_id = address.address_id AND address.phone = '1234' RETURN customer"; //OK
	        //String queryCypher ="MATCH (customer:customer), (address:address), (city:city) WHERE customer.address_id = address.address_id AND address.city_id = city.city_id AND city.city = 'Roma' RETURN customer"; //OK
		
			//String querySQL = "SELECT * FROM address, customer, city WHERE customer.address_id = address.address_id AND address.city_id = city.city_id"; //OK		
			//String querySQL = "SELECT * FROM customer, store WHERE customer.store_id = store.store_id AND store.manager_staff_id = 4"; //OK
			
			//String querySQL = "SELECT * FROM store, staff WHERE store.manager_staff_id = staff.staff_id AND staff.last_name = 'Giannini'"; //NOooo. Non funge con collegamenti bidirezionali (bug da risolvere)
			//String querySQL = "SELECT * FROM payment, staff WHERE payment.staff_id = staff.staff_id AND staff.last_name = 'Giannini'"; //OK
			//String querySQL = "SELECT * FROM store, address WHERE store.address_id = address.address_id AND address.address = 'via sala'";//OK
			
			//String querySQL = "SELECT * FROM store, staff WHERE staff.store_id = store.store_id AND store.manager_staff_id = 4"; //OK
		
		
		
		

		ParserNeo4j parser = new ParserNeo4j();
		parser.spezza(queryCypher);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		System.out.println("json scaricati: " + jsonUtili);
		List<String> tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabelle, jsonUtili);
		while(tabellePriorità.size()>1){
			tabellePriorità = caricatoreDAFile.getTabellaPrioritaAlta(tabellePriorità, jsonUtili);
		}
		String tabellaPrioritàAlta = tabellePriorità.get(0);
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
        
	
	}

}
