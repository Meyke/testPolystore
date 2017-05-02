package test;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UnitoreColonne {

	public JsonArray unisciColonne(JsonArray risultati, JsonArray risQueryPrec,String foreignKey, String tabellaDaUnire) {
		System.out.println("RISULTATI QUESTA TABELLA" + risultati.toString());
		System.out.println("RISULTATI PRECEDENTI" + risQueryPrec.toString());
		System.out.println("FOREIGN KEY " + foreignKey);
		System.out.println("TABELLA DA UNIRE " + tabellaDaUnire);
		JsonArray risultatiUniti = new JsonArray();
		String s = foreignKey.split("\\.")[1];
		String primaryKeyTabellaDaUnire = tabellaDaUnire+"."+s;
		for(int i=0; i<risultati.size(); i++){
			JsonObject rigaRisultatiQuestaTabella = risultati.get(i).getAsJsonObject();
			for(int j=0; j<risQueryPrec.size(); j++){
				JsonObject rigaRisultatiTabellaDaUnire = risQueryPrec.get(j).getAsJsonObject();
				String a = rigaRisultatiQuestaTabella.get(foreignKey).getAsString();
				a = a.split("\\.")[0];
				System.out.println("a: "+a);
				String b = rigaRisultatiTabellaDaUnire.get(primaryKeyTabellaDaUnire).getAsString();
				System.out.println("b: "+b);
				b = b.split("\\.")[0]; //quest perchè ho messo le primary key alla ca**o. mettere meglio le pk
				System.out.println("a.equals(b): "+a.equals(b));
				if(a.equals(b)){
					JsonObject rigaUnita = unisci(rigaRisultatiQuestaTabella,rigaRisultatiTabellaDaUnire);
					risultatiUniti.add(rigaUnita);
				}
						
			
			}
		}
		System.out.println("RISULTATI UNITI: " + risultatiUniti);
		return risultatiUniti;
	}

	private JsonObject unisci(JsonObject rigaRisultatiQuestaTabella, JsonObject rigaRisultatiTabellaDaUnire) {
		Set<Map.Entry<String, JsonElement>> entries = rigaRisultatiTabellaDaUnire.entrySet();//ritorna i membri del jsonObject
		for (Map.Entry<String, JsonElement> entry: entries) {
		    String chiave = entry.getKey();
		    rigaRisultatiQuestaTabella.addProperty(chiave, rigaRisultatiTabellaDaUnire.get(chiave).getAsString());
		}
		return rigaRisultatiQuestaTabella;
	}

}
