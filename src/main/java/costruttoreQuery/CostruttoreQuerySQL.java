package costruttoreQuery;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import postgres.RelationalDao;
import utility.Convertitore;
import utility.GestoreRisultato;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * La classe ha il compito di creare una query SQL prendendo come spunto le informazioni sulla tabella di
 * Postgres da interrogare, le condizioni associate a quella tabella e i risultati di query precedenti
 * @author micheletedesco1
 *
 */
public class CostruttoreQuerySQL implements CostruttoreQuery{
	/**
	 * Il metodo serve a costruire la query Postgres.
	 * @param myJson un oggetto json che contiene informazioni su quella tabella (prelevate da un file per ora)
	 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
	 * @param mappaWhere una mappa con key = tabella e value = condizioni per quella tabella
	 * @param tabellaKnows contiene informazioni sulla tabella di join (se presente)
	 * @param jsonUtili contiene informazioni sulle tabelle interrogare
	 */	
	@Override
	public JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows, Map<String, JsonObject> jsonUtili) throws Exception{
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		String tabellaDaUnire = null;
		System.out.println("DEVO ESEGUIRE LA TABELLA: " + tabella);
		String queryBase = "SELECT * FROM "+ tabella + " WHERE 1=1";
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		System.out.println("CONDIZIONI PER QUELLA TABELLA: " + condizioniPerQuellaTabella.toString());
		
		
		StringBuilder queryRiscritta = new StringBuilder();
		System.out.println("QUERY BASE: " + queryBase);
		queryRiscritta.append(queryBase);
		//adesso devo effettuare un controllo su ogni riga della matrice e poi costruire la query
		for (int i=0; i<condizioniPerQuellaTabella.size();i++){ //potevo usare un for each
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			//effettuo un controllo per vedere se quella è una riga di join o meno
			
			//System.out.println(condizione.get(0));
			//System.out.println(tabellaKnows.get("foreignkey").getAsString());
			if (!condizione.get(0).equals(tabellaKnows.get("foreignkey").getAsString())){ //da aggiungere<----------------
				//se non è una condizione di join, la appendo direttamente alla query riscritta
				String sottoStringa = " AND " + condizione.get(0) + " " + condizione.get(2) + " " + condizione.get(1);
				queryRiscritta.append(sottoStringa);
				System.out.println("QUERY RISCRITTA: " + queryRiscritta.toString());
			}
			else{
				//altrimenti è richiesto un join. Setto a true la variabile richiestaJoin. Generalizzare se più join 
				tabellaDaUnire = tabellaKnows.get("table").getAsString();
				richiestaJoin = true;
				parametroJoin = condizione.get(0);
				valueJoin = condizione.get(1);	
			}
			if (richiestaJoin == true)
				risultato = effettuaJoin(queryRiscritta, risQueryPrec, parametroJoin, valueJoin, tabella, tabellaDaUnire, jsonUtili);
			else{
				risultato = eseguiQueryDirettamente(queryRiscritta, tabella);
				//System.out.println(risultato.toString());
			}
			
		}		
		if (condizioniPerQuellaTabella.size() == 0){
			risultato = eseguiQueryDirettamente(queryRiscritta, tabella);
		}
		System.out.println("RISULTATO TEMPORANEO: " + risultato.toString());
		return risultato;
	}
	
	/**
	 * Il metodo serve a effettuare il join, ovvero legare i risultati della query precedente alla query in corso di sviluppo
	 * @param queryRiscritta
	 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
	 * @param parametroJoin la foreign key della tabella attuale (es. customer.store_id)
	 * @param valueJoin il valore della foreign key della tabella attuale (es. store.store_id)
	 * @param tabella nome della tabella da interrogare
	 * @param tabellaDaUnire nome della tabella eseguita in precedenza e da cui ho ottenuto i risultati
	 */
	private JsonArray effettuaJoin(StringBuilder queryRiscritta, JsonArray risQueryPrec, String parametroJoin, String valueJoin, String tabella, String tabellaDaUnire, Map<String, JsonObject> jsonUtili) throws Exception{
		RelationalDao dao = new RelationalDao();
		JsonObject elementoRisultatoPrecedente;
		StringBuilder queryTemporanea;
		String sottoStringa;
		JsonArray risultati = new JsonArray();
		//System.out.println("valore join" + valueJoin);
		//effettuo il join un risultato per volta
		for (int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();//dovrei ottenere un jsonObject
			System.out.println("ELEMENTO " + i + " RISULTATO PRECEDENTE: " + elementoRisultatoPrecedente.toString());
			queryTemporanea = new StringBuilder().append(queryRiscritta);
			if(elementoRisultatoPrecedente.get(valueJoin)==null){
				String id =valueJoin.split("\\.")[1];
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(id).getAsString(); //da parametrizzare solo con valuejoin o primary key del join. ATTENZIONE
			}
			else
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(valueJoin).getAsString();
			queryTemporanea.append(sottoStringa);
			//eseguo la stringa passandola al client rpc
			System.out.println("QUERY INVIATA: " + queryTemporanea.toString());
			ResultSet rigaRisultato = dao.interroga(queryTemporanea.toString());
			JsonArray risultatiParziali = Convertitore.convertSQLToJSON(rigaRisultato, tabella);
			risultati = concatArray(risultati, risultatiParziali);
			//System.out.println(risultati.toString());
			//concateno i vari jsonArray
		}
		
		JsonArray risultatiUniti = GestoreRisultato.unisciColonne(risultati,risQueryPrec,parametroJoin, tabellaDaUnire, jsonUtili);
		return risultatiUniti; //devo ritornare il jsonArray
		
	}

	/**
	 * La query con join restituisce varie collezioni di righe, quindi vari jsonArray, che dovrò concatenare 
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
	 * @param tabella
	 */
	private JsonArray eseguiQueryDirettamente(StringBuilder queryRiscritta, String tabella) throws Exception{
		RelationalDao dao = new RelationalDao();
		ResultSet risultatoResultSet = dao.interroga(queryRiscritta.toString());
		JsonArray risultati = Convertitore.convertSQLToJSON(risultatoResultSet, tabella);
		return risultati;
	}

}
