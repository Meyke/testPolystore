package costruttoreQuery;


import java.util.List;
import java.util.Map;

import neo4j.GraphDao;

import org.neo4j.graphdb.Result;

import utility.Convertitore;
import utility.GestoreRisultato;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * La classe ha il compito di creare una query Cypher prendendo come spunto le informazioni sulla tabella di
 * Neo4j (nodo) da interrogare, le condizioni associate a quella tabella e i risultati di query precedenti
 * @author micheletedesco1
 *
 */
public class CostruttoreQueryNeo4j implements CostruttoreQuery {

	/**
	 * Il metodo serve a costruire la query Cypher.
	 * @param myJson un oggetto json che contiene informazioni su quella tabella (prelevate da un file per ora)
	 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
	 * @param mappaWhere una mappa con key = tabella e value = condizioni per quella tabella
	 * @param tabellaKnows contiene informazioni sulla tabella di join (se presente)
	 * @param jsonUtili contiene informazioni sulle tabelle interrogare
	 */	
	@Override
	public JsonArray eseguiQuery(JsonObject myJson, JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows, Map<String, JsonObject> jsonUtili) throws Exception {	
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		String tabellaDaUnire = null;
		System.out.println("eseguo: "+tabella);
		String queryBase = "MATCH ("+ tabella + ":" + tabella +")" + " WHERE 1=1";
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		System.out.println(condizioniPerQuellaTabella.toString());
		
		
		StringBuilder queryRiscritta = new StringBuilder();
		System.out.println("query base: " + queryBase);
		queryRiscritta.append(queryBase);
		System.out.println(condizioniPerQuellaTabella.size());
		//adesso devo effettuare un controllo su ogni riga della matrice e poi costruire la query
		for (int i=0; i<condizioniPerQuellaTabella.size();i++){ //potevo usare un for each
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			//effettuo un controllo per vedere se quella è una riga di join o meno
			
			System.out.println("CONDIZIONE: "+condizione.get(0));
			//System.out.println(myJson.get("foreignkey").getAsString());
			if (!condizione.get(0).equals(tabellaKnows.get("foreignkey").getAsString())){ //da aggiungere<----------------
				//se non è una condizione di join, la appendo direttamente alla query riscritta
				String sottoStringa = " AND " + condizione.get(0) + " = " + condizione.get(1);
				queryRiscritta.append(sottoStringa);
				System.out.println(queryRiscritta.toString());
			}
			else{
				//altrimenti è richiesto un join. Setto a true la variabile richiestaJoin. Generalizzare se più join 
				tabellaDaUnire = tabellaKnows.get("table").getAsString();
				richiestaJoin = true;
				parametroJoin = condizione.get(0);
				valueJoin = condizione.get(1);	
			}
			if (richiestaJoin == true)
				risultato = effettuaJoin(queryRiscritta, risQueryPrec, parametroJoin, valueJoin, tabella, myJson, tabellaDaUnire, jsonUtili);
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
				System.out.println(queryRiscritta.toString());
				risultato = eseguiQueryDirettamente(queryRiscritta);
				System.out.println(risultato.toString());
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
			System.out.println(queryRiscritta);
			risultato = eseguiQueryDirettamente(queryRiscritta);
		}
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
	private JsonArray effettuaJoin(StringBuilder queryRiscritta, JsonArray risQueryPrec, String parametroJoin, String valueJoin, String tabella, JsonObject myJson, String tabellaDaUnire, Map<String, JsonObject> jsonUtili) throws Exception{
		GraphDao dao = new GraphDao();
		JsonObject elementoRisultatoPrecedente;
		StringBuilder queryTemporanea;
		String sottoStringa;
		JsonArray risultati = new JsonArray();
		//effettuo il join un risultato per volta
		for (int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();//dovrei ottenere un jsonObject
			System.out.println(elementoRisultatoPrecedente.toString());
			queryTemporanea = new StringBuilder().append(queryRiscritta);
			StringBuilder membriReturn = new StringBuilder();
			//per ritornare tutti i membri (colonne) di quella tabella
			JsonArray membriTabella = myJson.getAsJsonArray("members");
			for (int iterator=0; iterator<membriTabella.size(); iterator++){
				String membro = membriTabella.get(iterator).getAsString();
				if (iterator == membriTabella.size()-1){
					membriReturn.append(membro);
				}
				else membriReturn.append(membro + ", ");
			}
			if(elementoRisultatoPrecedente.get(valueJoin)==null){
				String id =valueJoin.split("\\.")[1];
			    sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(id).getAsString() + " RETURN " + membriReturn; //da parametrizzato, altrimenti dava Node[225] per esempio
			}
			else
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(valueJoin).getAsString() + " RETURN " + membriReturn;
			
			queryTemporanea.append(sottoStringa);
			
			//eseguo la stringa passandola al client rpc---- da fare
			System.out.println(queryTemporanea.toString());
			Result rigaRisultato = dao.interroga(queryTemporanea.toString());
			JsonArray risultatiParziali = Convertitore.convertCypherToJSON(rigaRisultato);
			risultati = concatArray(risultati, risultatiParziali);
			System.out.println(risultati.toString());
			//concateno i vari jsonArray
		}
		dao.chiudiConnessione();
		JsonArray risultatiUniti = GestoreRisultato.unisciColonne(risultati,risQueryPrec,parametroJoin, tabellaDaUnire, jsonUtili);
		System.out.println("RISULTATI: " + risultatiUniti);
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
	
	/**Questo metodo serve ad eseguire la query Cypher direttamente, nel caso non contenga join
	 * @param queryRiscritta
	 * @param tabella
	 */
	private JsonArray eseguiQueryDirettamente(StringBuilder queryRiscritta) throws Exception{
		GraphDao dao = new GraphDao();
		Result risultatoResult = dao.interroga(queryRiscritta.toString());
		JsonArray risultati = Convertitore.convertCypherToJSON(risultatoResult);
		dao.chiudiConnessione(); //ultra importante, altrimenti avrei due connessioni sulla stessa porta. Conseguenza: collisione e quindi exception
		return risultati;
	}
}
