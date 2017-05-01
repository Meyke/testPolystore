package test;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GeneratoreRisultatiJoin {

	public JsonArray unisciRisultati(JsonArray risultatiPrimoMembro, JsonArray risultatiSecondoMembro,  Map<String, JsonObject> jsonUtili, String secondoMembro, String parametroJoin){
		System.out.println("\n\nGENERATORE RISULTATI JOIN\n\n");
		JsonArray attributiSecondoMembro = jsonUtili.get(secondoMembro).get("members").getAsJsonArray();
		System.out.println("ATTRIBUTI SECONDO MEMBRO.SIZE() " + attributiSecondoMembro.size()+ "\n");
		for(int i=0; i<risultatiPrimoMembro.size(); i++){
			JsonObject b = risultatiPrimoMembro.get(i).getAsJsonObject();
			for(int z=0; z<risultatiSecondoMembro.size(); z++){
				JsonObject a = risultatiSecondoMembro.get(z).getAsJsonObject();
				System.out.println("VERIFICA IF\na.get(parametroJoin) == b.get(parametroJoin)   : "+ a.get(parametroJoin).getAsLong()+" == "+b.get(parametroJoin).getAsLong()+"\n");
				System.out.println("ALTRO IF\na.get(parametroJoin.split) == b.get(parametroJoin)   : "+ a.get(parametroJoin.split("\\_")[0]+"."+parametroJoin).getAsLong()+" == "+b.get(parametroJoin).getAsLong()+"\n");
				if(a.get(parametroJoin).getAsLong() == b.get(parametroJoin).getAsLong() || a.get(parametroJoin.split("\\_")[0]+"."+parametroJoin).getAsLong() == b.get(parametroJoin).getAsLong()){ //la seconda condizione deriva dal fatto che mongo restituisce risultati numerici in formato differente rispetto agli altri DB
					for(int j=0; j<attributiSecondoMembro.size(); j++){
						String attrCorrente = attributiSecondoMembro.get(j).getAsString().split("\\.")[1];
						if(b.get(attrCorrente)==null && a.get(attrCorrente) != null)
							b.add(attrCorrente, a.get(attrCorrente));
						else if(b.get(attrCorrente) == null)
							b.add(attrCorrente, a.get(attributiSecondoMembro.get(j).getAsString()));

					}
				}
			}
		}
		return risultatiPrimoMembro;
	}
}
