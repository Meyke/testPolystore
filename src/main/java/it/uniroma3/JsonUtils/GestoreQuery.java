package it.uniroma3.JsonUtils;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Questa classe ha lo scopo di eseguire la query per adesso solo SQL. poi la estendo a tutto il resto
 * @author micheletedesco1
 *
 */
public class GestoreQuery {
	
	public JsonArray esegui (JsonObject questoJson, JsonArray risQueryPrec, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere){
		JsonArray risultati = null;
		JsonObject altroJson = jsonUtili.get(questoJson.get("Knows").getAsString());
		if (altroJson == null)
			risultati = eseguiQuery(questoJson, null, mappaWhere);
		else
			risultati = eseguiQuery(questoJson, esegui(altroJson, risQueryPrec, jsonUtili, mappaWhere), mappaWhere);
		return risultati;
				
	}
	//questo è un eseguiquerySQL. chiamarlo costruttoreQuerySql. Rivedere
	private JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere){
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato;
		String tabella = myJson.get("table").getAsString();
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		StringBuilder queryRiscritta = new StringBuilder();
		queryRiscritta.append(myJson.get("query").getAsString());
		
		//adesso devo effettuare un controllo su ogni riga della matrice e poi costruire la query
		for (int i=0; i<condizioniPerQuellaTabella.size();i++){ //potevo usare un for each
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			//effettuo un controllo per vedere se quella è una riga di join o meno
			if (!condizione.get(0).equals(myJson.get("foreignkey").getAsString())){ //da aggiungere<----------------
				//se non è una condizione di join, la appendo direttamente alla query riscritta
				String sottoStringa = " AND " + condizione.get(0) + " = " + condizione.get(1);
				queryRiscritta.append(sottoStringa);
			}
			else{
				//altrimenti è richiesto un join. Setto a true la variabile richiestaJoin. Generalizzare se più join 
				richiestaJoin = true;
				parametroJoin = condizione.get(0);
				valueJoin = condizione.get(1);	
			}
			if (richiestaJoin)
				risultato = effettuaJoin(queryRiscritta, risQueryPrec, parametroJoin, valueJoin);
			else{
				//eseguiqueryRiscritta passandola al client RPC di rabbitMQ. Mi deve ritornare un jsonArray
			}
			
			
		}		
		return null;
	}
	
	private JsonArray effettuaJoin(StringBuilder queryRiscritta, JsonArray risQueryPrec, String parametroJoin, String valueJoin){
		JsonObject elementoRisultatoPrecedente;
		StringBuilder queryTemporanea;
		String sottoStringa;
		//effettuo il join un risultato per volta
		for (int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i); //dovrei ottenere un jsonObject
			queryTemporanea = new StringBuilder().append(queryRiscritta);
			sottoStringa = " AND " + parametroJoin + " = " + elemetoRisultatoPrecedente.get(value).toString();
			queryTemporanea.append(sottoStringa);
			//eseguo la stringa passandola al client rpc
			//concateno i vari jsonArray
		}
		return null; //devo ritornare il jsonArray
		
		
		
		
		
	}
	
	

}
