package test;


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
		System.out.println(tabella);
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		System.out.println(condizioniPerQuellaTabella.toString());
		
		
		BasicDBObject query = new BasicDBObject();
		System.out.println(condizioniPerQuellaTabella.size());
		for(int i=0; i<condizioniPerQuellaTabella.size(); i++){
			//estraggo la riga i-esima della matrice
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			
			System.out.println(condizione.get(0));
			System.out.println(myJson.get("foreignkey").getAsString());
			//effettuo un controllo per vedere se quella è una riga di join o meno
			if(!condizione.get(0).equals(myJson.get("foreignkey").getAsString())){
				String parametro = condizione.get(0).replace(tabella+".", "");
				System.out.println("parametro: "+ parametro);
				String valore= condizione.get(1).replaceAll("'", "");
				//vedo se il valore è un numero o una stringa (mongoDB è poco intelligente sui tipi)
				Object value = null;
				if (Character.isDigit(valore.charAt(0)))
					 value = Integer.parseInt(valore);
				else value = valore;
				System.out.println("valore: "+ value);
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
				System.out.println(risultato.toString());
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
			System.out.println(elementoRisultatoPrecedente.toString());
			int valore = elementoRisultatoPrecedente.get("id").getAsInt(); //comunque la primary key se voglio parametrizzare
			DBObject queryTemporanea = new BasicDBObject();
			queryTemporanea.putAll(query);
			parametroJoin = parametroJoin.replace(tabella+".", "");
			queryTemporanea.put(parametroJoin, valore);
			System.out.println("query temporanea " + queryTemporanea.toString());
			JsonArray risultatiParziali = eseguiQueryDirettamente(queryTemporanea, tabella);
			risultati = concatArray(risultati, risultatiParziali);
			System.out.println(risultati.toString());
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
