package it.uniroma3.costruttoreQuery;


import it.uniroma3.persistence.mongo.MongoDao;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class CostruttoreQueryMongo implements CostruttoreQuery {

	@Override
	public JsonArray eseguiQuery(JsonObject myJson, JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere) throws Exception {
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		
		
		BasicDBObject query = new BasicDBObject();
		for(int i=0; i<condizioniPerQuellaTabella.size(); i++){
			//estraggo la riga i-esima della matrice
			List<String> condizione = condizioniPerQuellaTabella.get(i);

			//effettuo un controllo per vedere se quella è una riga di join o meno
			if(!condizione.get(0).equals(myJson.get("foreignkey").getAsString())){
				String parametro = condizione.get(0).replace(tabella+".", "");
				String valore= condizione.get(1).replaceAll("'", "");
				//vedo se il valore è un numero o una stringa (mongoDB è poco intelligente sui tipi)
				Object value = null;
				if (Character.isDigit(valore.charAt(0)))
					 value = Integer.parseInt(valore);
				else value = valore;
				query.put(parametro, value);
			}
			else{
				richiestaJoin = true; 
				parametroJoin = condizione.get(0);
				valueJoin = condizione.get(1);
			}
			if (richiestaJoin)
				risultato = effettuaJoin(query, risQueryPrec, parametroJoin, valueJoin, tabella);
			else{
				risultato = eseguiQueryDirettamente(query,tabella);
			}
		}
		return risultato;
	}

	private JsonArray eseguiQueryDirettamente(DBObject queryRiscritta, String tabella) throws Exception { 
		MongoDao dao = new MongoDao();
		DBCursor cursor = dao.interroga(queryRiscritta,tabella);
		JsonArray documenti = new JsonArray();
		JsonParser parser = new JsonParser();
		while (cursor.hasNext()){
			BasicDBObject oggetto = (BasicDBObject) cursor.next();
			String documentoInFormatoStringa = oggetto.toString();
			JsonObject documentoInFormatoJson = parser.parse(documentoInFormatoStringa).getAsJsonObject();	
			documenti.add(documentoInFormatoJson);
		}
		return documenti;
	}

	private JsonArray effettuaJoin(DBObject query, JsonArray risQueryPrec, String parametroJoin, String valueJoin, String tabella) throws Exception {
		JsonObject elementoRisultatoPrecedente;
		JsonArray risultati = new JsonArray();
		
		for(int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();
			int valore = elementoRisultatoPrecedente.get("id").getAsInt(); //comunque la primary key se voglio parametrizzare
			DBObject queryTemporanea = new BasicDBObject();
			queryTemporanea.putAll(query);
			parametroJoin = parametroJoin.replace(tabella+".", "");
			queryTemporanea.put(parametroJoin, valore);
			JsonArray risultatiParziali = eseguiQueryDirettamente(queryTemporanea, tabella);
			risultati = concatArray(risultati, risultatiParziali);
		}
		return risultati;
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
}
