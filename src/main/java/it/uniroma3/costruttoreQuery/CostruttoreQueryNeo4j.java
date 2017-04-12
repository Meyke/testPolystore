package it.uniroma3.costruttoreQuery;


import it.uniroma3.JsonUtils.Convertitore;
import it.uniroma3.persistence.neo4j.GraphDao;

import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Questa classe ha il compito di creare una query Cypher prendendo come spunto la tabella in 
 * neo4j da interrogare, le condizioni associate a quella tabella e i risultati di query precedenti
 * @author micheletedesco1
 *
 */

public class CostruttoreQueryNeo4j implements CostruttoreQuery {
/**
 * Questo metodo serve a costruire la query Cypher.
 * @param myJson un oggetto json che contiene informazioni su quella tabella (prelevate da un file per ora)
 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
 * @param mappaWhere una mappa con key = tabella e value = condizioni per quella tabella
 */
	@Override
	public JsonArray eseguiQuery(JsonObject myJson, JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere) throws Exception {	
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);	
		StringBuilder queryRiscritta = new StringBuilder();
		queryRiscritta.append(myJson.get("query").getAsString());
		
		//adesso devo effettuare un controllo su ogni riga della matrice (mappa di liste) e poi costruire la query
		for (int i=0; i<condizioniPerQuellaTabella.size();i++){ 
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			//effettuo un controllo per vedere se quella è una riga di join o meno
			
			if (!condizione.get(0).equals(myJson.get("foreignkey").getAsString())){ 
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
			if (richiestaJoin == true)
				risultato = effettuaJoin(queryRiscritta, risQueryPrec, parametroJoin, valueJoin, tabella);
			else{
				StringBuilder membriReturn = new StringBuilder();
				JsonArray membriTabella = myJson.getAsJsonArray("members");
				for (int iterator=0; iterator<membriTabella.size(); iterator++){
					String membro = membriTabella.get(iterator).getAsString();
					if (iterator == membriTabella.size()-1){
						membriReturn.append(membro);
					}
					else membriReturn.append(membro + ", ");
				}
				queryRiscritta.append(" RETURN " + membriReturn);// per ritornare tutti i campi della tabella neo4j
				risultato = eseguiQueryDirettamente(queryRiscritta);
			}
			
			
		}	
		if (condizioniPerQuellaTabella.size() == 0){
			StringBuilder membriReturn = new StringBuilder();
			JsonArray membriTabella = myJson.getAsJsonArray("members");
			for (int iterator=0; iterator<membriTabella.size(); iterator++){
				String membro = membriTabella.get(iterator).getAsString();
				if (iterator == membriTabella.size()-1){
					membriReturn.append(membro);
				}
				else membriReturn.append(membro + ", ");
			}
			queryRiscritta.append(" RETURN " + membriReturn);// per ritornare tutti i campi della tabella neo4j
			risultato = eseguiQueryDirettamente(queryRiscritta);
		}
		return risultato;
	}
	
	/**
	 * Questo metodo serve a effettuare il join, ovvero legare i risultati della query precedente alla query in corso di sviluppo
	 * @param queryRiscritta
	 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
	 * @param parametroJoin la foreign key della tabella attuale (es. customer.store_id)
	 * @param valueJoin il valore della foreign key della tabella attuale (es. store.store_id)
	 */
	private JsonArray effettuaJoin(StringBuilder queryRiscritta, JsonArray risQueryPrec, String parametroJoin, String valueJoin, String tabella) throws Exception{
		GraphDao dao = new GraphDao();
		JsonObject elementoRisultatoPrecedente;
		StringBuilder queryTemporanea;
		String sottoStringa;
		JsonArray risultati = new JsonArray();
		//effettuo il join un risultato per volta
		for (int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();//dovrei ottenere un jsonObject
			queryTemporanea = new StringBuilder().append(queryRiscritta);
			if(elementoRisultatoPrecedente.get(valueJoin)==null)
			    sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get("id").getAsString() + " RETURN " + tabella +".id"; //da parametrizzare con tutti i campi, altrimenti restituisce Node[225] per esempio
			else
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(valueJoin).getAsString() + " RETURN " + tabella +".id";
			
			queryTemporanea.append(sottoStringa);
			
			//sparo la query (è una stringa) alla dao che si occuperà di lanciarla al database
			Result rigaRisultato = dao.interroga(queryTemporanea.toString());
			JsonArray risultatiParziali = Convertitore.convertCypherToJSON(rigaRisultato);
			risultati = concatArray(risultati, risultatiParziali);
			//concateno i vari jsonArray
		}
		return risultati; //devo ritornare il jsonArray
		
	}

	/**La query con join restituisce varie collezioni di righe, quindi vari jsonArray, che dovrò concatenare
	 * 
	 */
	private JsonArray concatArray(JsonArray arr1, JsonArray arr2){
	    JsonArray result = new JsonArray();
	    for (int i = 0; i < arr1.size(); i++) {
	        result.add(arr1.get(i));
	    }
	    for (int i = 0; i < arr2.size(); i++) {
	        result.add(arr2.get(i));
	    }
	    return result;
	}
	
	/**Questo metodo serve ad eseguire la query Cypher direttamente, nel caso non contenga join
	 * @param queryRiscritta
	 */
	private JsonArray eseguiQueryDirettamente(StringBuilder queryRiscritta) throws Exception{
		GraphDao dao = new GraphDao();
		Result risultatoResult = dao.interroga(queryRiscritta.toString());
		JsonArray risultati = Convertitore.convertCypherToJSON(risultatoResult);
		return risultati;
	}
}
