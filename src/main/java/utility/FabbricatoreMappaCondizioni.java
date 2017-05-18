package utility;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.JSQLParserException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * La classe permette di associare ogni condizioni scritta nella clausula WHERE alla rispettiva tabella da interrogare
 * @author micheletedesco1
 *
 */
public class FabbricatoreMappaCondizioni {
	private Map<String, List<List<String>>> mappaWhere;


	public FabbricatoreMappaCondizioni() {
		this.mappaWhere = new HashMap<>();
	}


	public void creaMappaWhere(List<List<String>> matriceWhere, Map<String, JsonObject> jsonCheMiServono ) throws UnknownHostException, FileNotFoundException, JSQLParserException {


		JsonObject myjson = new JsonObject();
		Set<String> tabelle = jsonCheMiServono.keySet();

		//System.out.println("TABELLE DA ELABORARE: " + tabelle.toString());
		for (String s : tabelle){
			List<List<String>> matriceWherePreciso = new LinkedList<>();
			myjson = jsonCheMiServono.get(s);
			JsonArray attributi = myjson.getAsJsonArray("members");
			for(int i=0; i<attributi.size(); i++){
				String attributo = attributi.get(i).getAsString();	
				for(List<String> rigaMatriceWhere : matriceWhere){
					if (attributo.equals(rigaMatriceWhere.get(0))){
						condizioneDiJoin(matriceWhere, tabelle, jsonCheMiServono, myjson, rigaMatriceWhere.get(0));
						matriceWherePreciso.add(rigaMatriceWhere);
					}
				}
			}
			mappaWhere.put(s, matriceWherePreciso);
		}
		aggiustaMappa(mappaWhere, tabelle);
	}

/*
 * in modo da poter scrivere customer.address_id = address.address_id 
 * e address.address_id = customer.address_id , cioè '=' bidirezionale. 
 */
	private void aggiustaMappa(Map<String, List<List<String>>> mappaWhere,Set<String> tabelle) {
		int posizione = 0;
		for (String t : tabelle){
			List<List<String>> righeMappa = mappaWhere.get(t);
			int cont = -1;
			boolean rimuovi = false;
			for (List<String> rigaIesima : righeMappa){
				cont ++;
				String elemento = rigaIesima.get(0).split("\\.")[0];
				if (!t.equals(elemento)){
					posizione = cont;
					rimuovi = true;
					
				}
			}
			if (rimuovi == true)
				righeMappa.remove(posizione); 
		}
		
	}


	private void condizioneDiJoin(List<List<String>> matriceWhere, Set<String> tabelle, Map<String, JsonObject> jsonUtili, JsonObject questojson, String elemento) {
		//System.out.println("TABELLA CHE ELABORO: " + questojson.get("table").getAsString());
		JsonArray entitaCheConosce = questojson.get("knows").getAsJsonArray();
		boolean verifica = false;
		for (int j=0; j<entitaCheConosce.size(); j++){
			String fkTabellaCheConosce = entitaCheConosce.get(j).getAsJsonObject().get("foreignkey").getAsString();
			if (fkTabellaCheConosce.equals(elemento)){
				verifica = true;
			}
		}
		if (verifica == false){
			for (String s : tabelle){
				JsonObject myjson = jsonUtili.get(s);
				JsonArray attributi = myjson.getAsJsonArray("members");
				for(int i=0; i<attributi.size(); i++){
					String attributo = attributi.get(i).getAsString();	
					for(List<String> rigaMatriceWhere : matriceWhere){
						if (attributo.equals(rigaMatriceWhere.get(1))){
							//vuol dire che rigaMAtrice [1] è un elemento di join	
							boolean controllo = false;
							for (int j=0; j<entitaCheConosce.size(); j++){
								String fkTabellaCheConosce = entitaCheConosce.get(j).getAsJsonObject().get("foreignkey").getAsString();
								if (fkTabellaCheConosce.equals(rigaMatriceWhere.get(0))){ //se non lo conosce è fasullo
									controllo = true;
								}
							}
							//System.out.println("VALORE CONTROLLO" + controllo);
							if (controllo == false){ //inverto
								String el0 = rigaMatriceWhere.get(0);
								String el1 = rigaMatriceWhere.get(1);
								rigaMatriceWhere.set(0, el1);
								rigaMatriceWhere.set(1, el0);
							}
						}
					}
				}	
			}
		}

	}



	public Map<String, List<List<String>>> getMappaWhere() {
		return mappaWhere;
	}


	public void setMappaWhere(Map<String, List<List<String>>> mappaWhere) {
		this.mappaWhere = mappaWhere;
	}



}
