package test;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Result;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//la tabella non deve avere celle vuote
public class Convertitore {

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
	
	public static JsonArray convertCypherToJSON(Result result) throws Exception {
		JsonArray jsonArray = new JsonArray();
		while ( result.hasNext() ) {
        	Map<String, Object> row = result.next();
        	JsonObject obj = new JsonObject();
            for ( String key : result.columns() )
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

