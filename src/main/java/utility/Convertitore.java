package utility;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
/**
 * Classe che permette di convertire i risultati di una query in un formato comune. Come formato si e' usato JsonArray.
 * Ogni elemento del JsonArray e' un JsonObject, assibilabile a una riga di risultato.
 * @author micheletedesco1
 *
 */
public class Convertitore {
	
	/**
	 * Metodo statico che converte un ResultSet (SQL) in JsonArray
	 * @param resultSet risultato query SQL
	 * @param tabella nome tabella interrogata
	 * @return
	 * @throws Exception
	 */
	public static JsonArray convertSQLToJSON(ResultSet resultSet, String tabella) throws Exception {
		JsonArray jsonArray = new JsonArray();
		while (resultSet.next()) {
			int total_rows = resultSet.getMetaData().getColumnCount();
			JsonObject obj = new JsonObject();
			for (int i = 0; i < total_rows; i++) {
				obj.addProperty(tabella+"."+resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1).toString());
			    //System.out.println
			}
			jsonArray.add(obj);
		}
		return jsonArray;
	}
	
	/**
	 * Metodo statico che converte un Result (Cypher) in JsonArray
	 * @param result risultato query Cypher
	 * @return
	 * @throws Exception
	 */
	public static JsonArray convertCypherToJSON(StatementResult result) throws Exception {
		JsonArray jsonArray = new JsonArray();
		while ( result.hasNext() ) {
			Record record = result.next();
        	Map<String, Object> row = record.asMap();
        	JsonObject obj = new JsonObject();
            for ( String key : result.keys() )
            { obj.addProperty(key,row.get(key).toString());
            }
            jsonArray.add(obj);
        }
		return jsonArray;
	}
	
	public static JsonArray convertMongoToJSON(JsonArray documenti, String tabella) throws Exception {
		JsonArray documentiRiscritti = new JsonArray();
		for (int i=0; i<documenti.size(); i++){
			JsonObject rigaRiscritta = new JsonObject();
			JsonObject riga = documenti.get(i).getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> entries = riga.entrySet();//ritorna i membri del jsonObject
			for (Map.Entry<String, JsonElement> entry: entries) {
			    String chiave = entry.getKey();
			    String chiaveRiscritta = tabella+"."+chiave;
			    rigaRiscritta.add(chiaveRiscritta, riga.get(chiave));
			}
			documentiRiscritti.add(rigaRiscritta);
		}
		return documentiRiscritti;
		
	}

}

