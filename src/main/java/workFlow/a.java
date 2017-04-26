package workFlow;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;




public class a {
	public static void main (String[] args){
		Type listType = new TypeToken<List<String>>() {}.getType();
		System.out.println(listType.toString());
		List<String> lista = new LinkedList<>();
		lista.add("michele");
		lista.add("armando");
		Gson gson = new Gson(); // Or use new GsonBuilder().create();
		String json = gson.toJson(lista); // serializes target to Json
		List<String> target2 = gson.fromJson(json, listType);
		System.out.println(target2.get(0));
		/*
		 * Gson gson = new Gson(); // Or use new GsonBuilder().create();
           MyType target = new MyType();
           String json = gson.toJson(target); // serializes target to Json
           MyType target2 = gson.fromJson(json, MyType.class);// deserializes json into target2 
        
        *
        *Type listType = new TypeToken<Map<String, List<List<String>>>>() {}.getType();
        *
        */
		
		JsonArray risQueryPrec = new JsonArray();
		JsonObject messaggioJson = new JsonObject();
		messaggioJson.add("risQueryPrec", risQueryPrec);
		JsonArray jsonArray = messaggioJson.get("risQueryPrec").getAsJsonArray();
		System.out.println(jsonArray.size());
	}

}
