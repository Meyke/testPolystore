package model;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.ParserSql;
import utility.CaricatoreJSON;
import utility.FabbricatoreMappaCondizioni;
import utility.GestoreRisultato;
import utility.Orchestrator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Classe Facade. E' il punto di accesso al software sottostante. Data la query iniziale, si preoccupa di instanziare
 * il parser SQL, la mappaWhere, i jsonUtili, le tabelle e inizializzare l' orchestrator.
 * Alla fine delle varie operazioni ritorna il risultato finale della query (pattern facade)
 * @author micheletedesco1
 *
 */
public class FacadeSQL {
	
	public JsonArray gestisciQuery(String querySQL) throws Exception{
		ParserSql parser = new ParserSql();
		parser.spezza(querySQL);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		//System.out.println("MATRICE WHERE :" + matriceWhere);
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
		risultato = GestoreRisultato.proietta(parser.getListaProiezioni(), risultato, jsonUtili);
		return risultato;
		
	}

	/*
	 * i jsonUtili possono contenere informazioni superflue, da eliminare in base al contenuto della mappaWhere.
	 * Io ho sostituito le informazioni superflue mettendo "nessuno", ma per future ottimizzazioni è meglio eliminarle del tutto
	 */
	private Map<String, JsonObject> modificaJsonUtili(Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) {
		//System.out.println(jsonUtili.toString());
		Map<String, JsonObject> jsonUtiliModificati = new HashMap<>();
		Set<String> tabelle = jsonUtili.keySet();
		for (String t : tabelle){
			//System.out.println("TABELLA: " + t);
			JsonObject questoJson = jsonUtili.get(t).getAsJsonObject();
			JsonArray entitaCheConosce = questoJson.get("knows").getAsJsonArray();
			JsonArray entitaCheConosceModificate = new JsonArray();
			for (int i =0; i<entitaCheConosce.size();i++){
				JsonObject tabellaKnows = entitaCheConosce.get(i).getAsJsonObject();
				String fkTabellaCheConosce = entitaCheConosce.get(i).getAsJsonObject().get("foreignkey").getAsString();
				JsonObject altroJson = jsonUtili.get(tabellaKnows.get("table").getAsString());
				//System.out.println("TABELLA CHE CONOSCE: " +tabellaKnows.get("table").getAsString());
				//System.out.println("CONDIZIONE PRESENZA FIGLIO: "+ (((altroJson != null) && (GestoreRisultato.controlloFK(questoJson,fkTabellaCheConosce,mappaWhere)==true))));
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
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		String querySQL = "SELECT * FROM customer, store, address WHERE customer.store_id = store.store_id AND store.address_id = address.address_id AND address.address_id = 1";
		String query2 = "SELECT customer.first_name FROM rental, inventory, film, customer WHERE customer.customer_id = rental.customer_id and rental.inventory_id = inventory.inventory_id and inventory.film_id = film.film_id";
		FacadeSQL facade = new FacadeSQL();
		facade.gestisciQuery(querySQL);
		System.out.println("---------------");
		facade.gestisciQuery(query2);
		String query3 = "SELECT customer.first_name, payment.amount FROM customer, address, payment WHERE customer.address_id = address.address_id and payment.customer_id = customer.customer_id and payment.amount > 20";
		System.out.println("---------------");
		facade.gestisciQuery(query3);
		
	}
	
}
