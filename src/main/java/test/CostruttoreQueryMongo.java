package test;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
		System.out.println(myJson.get("query").getAsString());
		System.out.println(condizioniPerQuellaTabella.size());
		for(int i=0; i<condizioniPerQuellaTabella.size(); i++){
			//estraggo la riga i-esima della matrice
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			
			System.out.println(condizione.get(0));
			System.out.println(myJson.get("foreignkey").getAsString());
			//effettuo un controllo per vedere se quella è una riga di join o meno
			if(!condizione.get(0).equals(myJson.get("foreignKey").getAsString())){
				String parametro = condizione.get(0);
				String valore= condizione.get(1);
				query.put(parametro, valore);
			}
			else{
				richiestaJoin = true;
				parametroJoin = condizione.get(0);
				valueJoin = condizione.get(1);
			}
			if (richiestaJoin)
				risultato = effettuaJoin(query, risQueryPrec, parametroJoin, valueJoin);
			else{
				risultato = eseguiQueryDirettamente(query);
				System.out.println(risultato.toString());
			}
		}
		return risultato;
	}

	private JsonArray eseguiQueryDirettamente(DBObject queryRiscritta) {
		
		  queryRiscritta.put("number", 5);
		  DBCursor cursor = collection.find(queryRiscritta);
		  JsonArray risultato = new JsonArray();
		  while (cursor.hasNext()) {
// METODO ALTERNATIVO (usa JSONobject e non JsonObject)
//			 //assign the cursor to the DbObject
//			  DBObject result = cursor.next();
//			  //this line will convert the DbObject to JSONObject
//			  JsonObject output = new JSONObject(JSON.serialize(result));
			  
			  risultato.add(cursor.next().toString());
			System.out.println(cursor.next());
		  }
		  return risultato;
	}

	private JsonArray effettuaJoin(DBObject query, JsonArray risQueryPrec, String parametroJoin, String valueJoin) {
		JsonObject elementoRisultatoPrecedente;
		JsonArray risultati = new JsonArray();
		
		for(int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();
			System.out.println(elementoRisultatoPrecedente.toString());
			String valore = elementoRisultatoPrecedente.get(valueJoin).toString();
			DBObject queryTemporanea = (DBObject) query;
			queryTemporanea.put(parametroJoin, valore);
			System.out.println(queryTemporanea.toString());
			JsonArray risultatiParziali = eseguiQueryDirettamente(queryTemporanea);
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
