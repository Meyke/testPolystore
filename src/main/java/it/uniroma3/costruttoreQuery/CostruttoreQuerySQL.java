package it.uniroma3.costruttoreQuery;

import it.uniroma3.JsonUtils.Convertitore;
import it.uniroma3.persistence.postgres.RelationalDao;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Questa classe ha il compito di creare una query SQL prendendo come spunto la tabella in 
 * Postgres da interrogare, le condizioni associate a quella tabella e i risultati di query precedenti
 * @author micheletedesco1
 *
 */
public class CostruttoreQuerySQL implements CostruttoreQuery{
	/**
	 * Questo metodo serve a costruire la query Postgres.
	 * @param myJson un oggetto json che contiene informazioni su quella tabella (prelevate da un file per ora)
	 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
	 * @param mappaWhere una mappa con key = tabella e value = condizioni per quella tabella
	 */
	
	@Override
	public JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere) throws Exception{
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
				risultato = effettuaJoin(queryRiscritta, risQueryPrec, parametroJoin, valueJoin);
			else{
				risultato = eseguiQueryDirettamente(queryRiscritta);
			}
			
		}		
		if (condizioniPerQuellaTabella.size() == 0){
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
	private JsonArray effettuaJoin(StringBuilder queryRiscritta, JsonArray risQueryPrec, String parametroJoin, String valueJoin) throws Exception{
		RelationalDao dao = new RelationalDao();
		JsonObject elementoRisultatoPrecedente;
		StringBuilder queryTemporanea;
		String sottoStringa;
		JsonArray risultati = new JsonArray();
		//effettuo il join un risultato per volta
		for (int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();//dovrei ottenere un jsonObject
			queryTemporanea = new StringBuilder().append(queryRiscritta);
			if(elementoRisultatoPrecedente.get(valueJoin)==null)
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get("id").getAsString(); //da parametrizzare solo con valuejoin. ATTENZIONE
			else
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(valueJoin).getAsString();
			queryTemporanea.append(sottoStringa);
			//eseguo la stringa passandola al client rpc
			ResultSet rigaRisultato = dao.interroga(queryTemporanea.toString());
			JsonArray risultatiParziali = Convertitore.convertSQLToJSON(rigaRisultato);
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
	/**Questo metodo serve ad eseguire la query SQL direttamente, nel caso non contenga join
	 * @param queryRiscritta
	 */
	private JsonArray eseguiQueryDirettamente(StringBuilder queryRiscritta) throws Exception{
		RelationalDao dao = new RelationalDao();
		ResultSet risultatoResultSet = dao.interroga(queryRiscritta.toString());
		JsonArray risultati = Convertitore.convertSQLToJSON(risultatoResultSet);
		return risultati;
	}

}
