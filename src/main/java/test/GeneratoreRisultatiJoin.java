package test;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GeneratoreRisultatiJoin {
	
	public JsonArray unisciRisultati(JsonArray risultatiPrimoMembro, JsonArray risultatiSecondoMembro,  Map<String, JsonObject> jsonUtili, String secondoMembro){
		for( int i=0; i<risultatiPrimoMembro.size(); i++){
			JsonObject a = risultatiSecondoMembro.get(i).getAsJsonObject();
			JsonObject b = risultatiPrimoMembro.get(i).getAsJsonObject();
			JsonArray attributiSecondoMembro = jsonUtili.get(secondoMembro).get("members").getAsJsonArray();
			for( int j=0; j<attributiSecondoMembro.size(); j++){
				String attrCorrente = attributiSecondoMembro.get(j).getAsString().split("\\.")[1];
				if(b.get(attrCorrente)==null)
					b.add(attrCorrente, a.get(attrCorrente));
			}
		}
		return risultatiPrimoMembro;
	}
}
