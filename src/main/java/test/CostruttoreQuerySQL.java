package test;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CostruttoreQuerySQL implements CostruttoreQuery{
	
	
	//questo è un eseguiquerySQL. chiamarlo costruttoreQuerySql. Rivedere
	public JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere) throws Exception{
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		System.out.println(tabella);
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		System.out.println(condizioniPerQuellaTabella.toString());
		
		
		StringBuilder queryRiscritta = new StringBuilder();
		System.out.println(myJson.get("query").getAsString());
		queryRiscritta.append(myJson.get("query").getAsString());
		System.out.println(condizioniPerQuellaTabella.size());
		//adesso devo effettuare un controllo su ogni riga della matrice e poi costruire la query
		for (int i=0; i<condizioniPerQuellaTabella.size();i++){ //potevo usare un for each
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			//effettuo un controllo per vedere se quella è una riga di join o meno
			
			System.out.println(condizione.get(0));
			System.out.println(myJson.get("foreignkey").getAsString());
			if (!condizione.get(0).equals(myJson.get("foreignkey").getAsString())){ //da aggiungere<----------------
				//se non è una condizione di join, la appendo direttamente alla query riscritta
				String sottoStringa = " AND " + condizione.get(0) + " = " + condizione.get(1);
				queryRiscritta.append(sottoStringa);
				System.out.println(queryRiscritta.toString());
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
				System.out.println(risultato.toString());
			}
			
		}		
		if (condizioniPerQuellaTabella.size() == 0){
			risultato = eseguiQueryDirettamente(queryRiscritta);
		}
		return risultato;
	}
	
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
			System.out.println(queryTemporanea.toString());
			ResultSet rigaRisultato = dao.interroga(queryTemporanea.toString());
			JsonArray risultatiParziali = Convertitore.convertSQLToJSON(rigaRisultato);
			risultati = concatArray(risultati, risultatiParziali);
			System.out.println(risultati.toString());
			//concateno i vari jsonArray
		}
		return risultati; //devo ritornare il jsonArray
		
	}

	
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
	
	private JsonArray eseguiQueryDirettamente(StringBuilder queryRiscritta) throws Exception{
		RelationalDao dao = new RelationalDao();
		ResultSet risultatoResultSet = dao.interroga(queryRiscritta.toString());
		JsonArray risultati = Convertitore.convertSQLToJSON(risultatoResultSet);
		return risultati;
	}

}
