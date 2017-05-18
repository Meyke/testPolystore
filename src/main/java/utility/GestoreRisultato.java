package utility;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Classe che offre un insieme di metodi statici di utilita'.
 * @author micheletedesco1
 *
 */
public class GestoreRisultato {
/**
 * Metodo che dati i risultati della query appena eseguite e i risultati della qury precedente, permette di unire le colonne (join tra colonne)
 * @param risultati risultati della query che si sta eseguendo 
 * @param risQueryPrec risultati della query precedente
 * @param foreignKey
 * @param tabellaDaUnire
 * @param jsonUtili
 * @return
 */
	public static JsonArray unisciColonne(JsonArray risultati, JsonArray risQueryPrec,String foreignKey, String tabellaDaUnire, Map<String, JsonObject> jsonUtili) {
		JsonArray risultatiUniti = new JsonArray();
		//String s = foreignKey.split("\\.")[1];
		String primaryKeyTabellaDaUnire = jsonUtili.get(tabellaDaUnire).get("primarykey").getAsString();
		for(int i=0; i<risultati.size(); i++){
			JsonObject rigaRisultatiQuestaTabella = risultati.get(i).getAsJsonObject();
			for(int j=0; j<risQueryPrec.size(); j++){
				JsonObject rigaRisultatiTabellaDaUnire = risQueryPrec.get(j).getAsJsonObject();
				String a = rigaRisultatiQuestaTabella.get(foreignKey).getAsString();
				a = a.split("\\.")[0];
				
				String b = rigaRisultatiTabellaDaUnire.get(primaryKeyTabellaDaUnire).getAsString();
				
				b = b.split("\\.")[0]; //quest perchè ho messo le primary key alla ca**o. mettere meglio le pk
				
				if(a.equals(b)){
					JsonObject rigaUnita = unisci(rigaRisultatiQuestaTabella,rigaRisultatiTabellaDaUnire);
					risultatiUniti.add(rigaUnita);
				}
						
			
			}
		}
		return risultatiUniti;
	}

	private static JsonObject unisci(JsonObject rigaRisultatiQuestaTabella, JsonObject rigaRisultatiTabellaDaUnire) {
		Set<Map.Entry<String, JsonElement>> entries = rigaRisultatiTabellaDaUnire.entrySet();//ritorna i membri del jsonObject
		for (Map.Entry<String, JsonElement> entry: entries) {
		    String chiave = entry.getKey();
		    rigaRisultatiQuestaTabella.addProperty(chiave, rigaRisultatiTabellaDaUnire.get(chiave).getAsString());
		}
		return rigaRisultatiQuestaTabella;
	}

	/**
	 * Permette di eseguire la proiezione di un risultato
	 * @param listaProiezioni
	 * @param risultato
	 * @param jsonUtili
	 * @return
	 */
	public static JsonArray proietta(List<String> listaProiezioni, JsonArray risultato, Map<String,JsonObject> jsonUtili) {
		JsonArray risultatiProiettati = new JsonArray();
		// per gestire il caso *
		if ((listaProiezioni.isEmpty()) || (listaProiezioni.get(0).equals("*"))){
			return risultato;
		}
		for (int i=0; i<risultato.size();i++){
			JsonObject rigaRisultato = risultato.get(i).getAsJsonObject();
			JsonObject rigaProiettata = new JsonObject();
			for(String membro : listaProiezioni){
				//per gestire i casi come customer.*
				if(membro.contains("*")){
					String tabella = membro.split("\\.")[0];
					JsonArray colonne = jsonUtili.get(tabella).get("members").getAsJsonArray();
					for (int j=0; j<colonne.size();j++){
						rigaProiettata.addProperty(colonne.get(j).getAsString(),rigaRisultato.get(colonne.get(j).getAsString()).getAsString());
					}
				}
				else rigaProiettata.addProperty(membro, rigaRisultato.get(membro).getAsString());
			}
			risultatiProiettati.add(rigaProiettata);
		}
		return risultatiProiettati;
	}
	
	public static boolean controlloFK(JsonObject questoJson, String fkTabellaCheConosce,Map<String, List<List<String>>> mappaWhere ) {
		boolean controllo = false;
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(questoJson.get("table").getAsString());
		if (condizioniPerQuellaTabella.size()!=0){
			for (List<String> condizioneIESIMA : condizioniPerQuellaTabella){
				if(condizioneIESIMA.get(0).equals(fkTabellaCheConosce)){
					controllo = true;
				}
			}
		}
		return controllo;
	}

}
