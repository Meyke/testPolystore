package coseUtili;

import java.sql.ResultSet;
import java.util.Map;

import org.neo4j.graphdb.Result;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
//la tabella non deve avere celle vuote
public class Convertitore {

	public static JsonArray convertSQLToJSON(ResultSet resultSet) throws Exception {
		JsonArray jsonArray = new JsonArray();
		while (resultSet.next()) {
			int total_rows = resultSet.getMetaData().getColumnCount();
			JsonObject obj = new JsonObject();
			for (int i = 0; i < total_rows; i++) {
				obj.addProperty(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1).toString());
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

}

