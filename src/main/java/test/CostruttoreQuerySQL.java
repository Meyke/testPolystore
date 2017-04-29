package test;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CostruttoreQuerySQL implements CostruttoreQuery{
	
	
	//questo è un eseguiquerySQL. chiamarlo costruttoreQuerySql. Rivedere
	public JsonArray eseguiQuery(JsonObject myJson,JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows,  Map<String, JsonObject> jsonUtili) throws Exception{
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		System.out.println("Tabella presa da CostruttoreQuerySQL: "+tabella);
		String queryBase = "SELECT * FROM "+ tabella + " WHERE 1=1";
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		System.out.println("Condizioni per quella tabella: "+condizioniPerQuellaTabella.toString());
		
		
		StringBuilder queryRiscritta = new StringBuilder();
		System.out.println("query base: " + queryBase);
		queryRiscritta.append(queryBase);
		System.out.println("Condizioni per quella tabella.size :"+ condizioniPerQuellaTabella.size());
		//adesso devo effettuare un controllo su ogni riga della matrice e poi costruire la query
		for (int i=0; i<condizioniPerQuellaTabella.size();i++){ //potevo usare un for each
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			//effettuo un controllo per vedere se quella è una riga di join o meno
			
			System.out.println("La "+i+"-esima  condizione è sull'attributo: " +condizione.get(0));
			//System.out.println(tabellaKnows.get("foreignkey").getAsString());
			if (!condizione.get(0).equals(tabellaKnows.get("foreignkey").getAsString())){ //da aggiungere<----------------
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
				risultato = effettuaJoin(queryRiscritta, risQueryPrec, parametroJoin, valueJoin, jsonUtili);
			else{
				risultato = eseguiQueryDirettamente(queryRiscritta);
				System.out.println(risultato.toString());
			}
			
		}		
		if (condizioniPerQuellaTabella.size() == 0){
			risultato = eseguiQueryDirettamente(queryRiscritta);
		}
		System.out.println("RISULTATO PARZIALE: \n"+ risultato.toString()+"\n");
		return risultato;
	}
	
	private JsonArray effettuaJoin(StringBuilder queryRiscritta, JsonArray risQueryPrec, String parametroJoin, String valueJoin,  Map<String, JsonObject> jsonUtili) throws Exception{
		System.out.println("\nEFFETTUA JOIN  :\n");
		RelationalDao dao = new RelationalDao();
		JsonObject elementoRisultatoPrecedente;
		StringBuilder queryTemporanea;
		String sottoStringa;
		JsonArray risultatiPrimoMembro = new JsonArray();
		String secondoMembro = valueJoin.split("\\.")[0];
		System.out.println("valore join : " + valueJoin);
		//effettuo il join un risultato per volta
		for (int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();//dovrei ottenere un jsonObject
			queryTemporanea = new StringBuilder().append(queryRiscritta);
			if(elementoRisultatoPrecedente.get(valueJoin)==null){
				String id =valueJoin.split("\\.")[1];
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(id).getAsString(); //da parametrizzare solo con valuejoin o primary key del join. ATTENZIONE
			}
			else
				sottoStringa = " AND " + parametroJoin + " = " + elementoRisultatoPrecedente.get(valueJoin).getAsString();
			queryTemporanea.append(sottoStringa);
			//eseguo la stringa passandola al client rpc
			System.out.println("Query Temporanea "+i+"-esima su "+risQueryPrec.size()+" :" + queryTemporanea.toString());
			ResultSet rigaRisultato = dao.interroga(queryTemporanea.toString());
			JsonArray risultatiParziali = Convertitore.convertSQLToJSON(rigaRisultato);
			risultatiPrimoMembro = concatArray(risultatiPrimoMembro, risultatiParziali);
			System.out.println(risultatiPrimoMembro.toString()+"\n");
			//concateno i vari jsonArray
		}
		GeneratoreRisultatiJoin g = new GeneratoreRisultatiJoin();
		JsonArray risultati = g.unisciRisultati(risultatiPrimoMembro, risQueryPrec, jsonUtili, secondoMembro);
		System.out.println("RISULTATI SECONDO MEMBRO : \n"+risQueryPrec.toString()+"\n");
		return risultati;//devo ritornare il jsonArray
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
