package neo4j;


import java.util.List;
import java.util.Map;

import neo4j.persistence.GraphDao;

import org.neo4j.graphdb.Result;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import coseUtili.Convertitore;




public class CostruttoreQueryNeo4j {

	public JsonArray eseguiQuery(JsonObject myJson, JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows) throws Exception {	
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		System.out.println(tabella);
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
			
			System.out.println(condizione.get(0));
			//System.out.println(myJson.get("foreignkey").getAsString());
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
				risultato = effettuaJoin(queryRiscritta, risQueryPrec, parametroJoin, valueJoin, tabella, myJson);
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
	
	private JsonArray effettuaJoin(StringBuilder queryRiscritta, JsonArray risQueryPrec, String parametroJoin, String valueJoin, String tabella, JsonObject myJson) throws Exception{
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
		GraphDao dao = new GraphDao();
		Result risultatoResult = dao.interroga(queryRiscritta.toString());
		JsonArray risultati = Convertitore.convertCypherToJSON(risultatoResult);
		dao.chiudiConnessione(); //ultra importante, altrimenti avrei due connessioni sulla stessa porta. Conseguenza: collisione e quindi exception
		return risultati;
	}
}
