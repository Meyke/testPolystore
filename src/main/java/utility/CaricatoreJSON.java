package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//testato e funzionante attenzione alle virgolette, che cambiano nel file

/**
 * Questa classe ha il compito di prelevare le informazioni sulle tabelle da interrogare da qualche parte.
 * Allo scopo e' stato utilizzato un file di testo contenente queste informazioni come stringhe con sintassi json,
 * in modo poi da poter facilmente convertire in jsonObject veri e propri.
 * @author micheletedesco1
 *
 */
public class CaricatoreJSON {
	private Map<String,JsonObject> jsonCheMiServono;
	public CaricatoreJSON() {
		this.jsonCheMiServono = new HashMap<>();
	}

	public void caricaJSON(List<String> listaFrom) throws FileNotFoundException{
		ClassLoader classLoader = getClass().getClassLoader();
		File fileJSON = new File(classLoader.getResource("fileJSON.txt").getFile());
		Scanner scanner = new Scanner(fileJSON);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			for(String tabella : listaFrom){			
				JsonParser parser = new JsonParser();
				JsonObject myJson = parser.parse(line).getAsJsonObject();
				String table = myJson.get("table").getAsString();
				if(table.equals(tabella)){
				    this.jsonCheMiServono.put(tabella, myJson);
				}


			}
		}
		scanner.close();
	}


	public Map<String, JsonObject> getJsonCheMiServono() {
		return jsonCheMiServono;
	}

	public void setJsonCheMiServono(Map<String, JsonObject> jsonCheMiServono) {
		this.jsonCheMiServono = jsonCheMiServono;
	}

	
	/**
	 * La tabella con priorita' piu' alta coincide con la root table dell'albero da interrogare
	 * @param tabelle
	 * @param jsonUtili
	 * @param mappaWhere
	 * @return
	 */
	public List<String> getTabellaPrioritaAlta(List<String> tabelle, Map<String, JsonObject> jsonUtili, Map<String, List<List<String>>> mappaWhere) {
		//String tabellaPreferita = tabelle.get(0);
		//System.out.println(tabelle);
		if (tabelle.size()==1)
			return tabelle;
		List<String> tabellePreferite = new LinkedList<>();
		if (allEquals(tabelle) == true){
			tabellePreferite.add(tabelle.get(0));		
		}
			
		//tabellePreferite.add(tabellaPreferita);
		for(int i=0; i<tabelle.size();i++){
			JsonObject oggetto = jsonUtili.get(tabelle.get(i)); //es customer
			JsonArray knows = oggetto.get("knows").getAsJsonArray(); //es store e address
			for(int j=0; j<tabelle.size();j++){
				String tabella = tabelle.get(j);//customer
				for (int y=0; y<knows.size();y++){
					JsonObject tableKnows = knows.get(y).getAsJsonObject();//es store
					String fkTabellaCheConosce = tableKnows.get("foreignkey").getAsString();
					if ((tableKnows.get("table").getAsString().equals(tabella))&&(controlloFK(oggetto,fkTabellaCheConosce,mappaWhere)==true)){//vuol dire che la conosce
						String tabellaPreferita = oggetto.get("table").getAsString();
						tabellePreferite.add(tabellaPreferita);		
					}
				}
			}
		}	
		//System.out.println(tabellePreferite);
		return tabellePreferite;
	}

	private boolean controlloFK(JsonObject oggetto, String fkTabellaCheConosce, Map<String, List<List<String>>> mappaWhere) {
		boolean controllo = false;
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(oggetto.get("table").getAsString());
		if (condizioniPerQuellaTabella.size()!=0){
			for (List<String> condizioneIESIMA : condizioniPerQuellaTabella){
				if(condizioneIESIMA.get(0).equals(fkTabellaCheConosce)){
					controllo = true;
				}
			}
		}
		return controllo;
	}

	private boolean allEquals(List<String> tabelle) {
		boolean allEqual = true;
		for (String s : tabelle) {
		    if(!s.equals(tabelle.get(0)))
		        allEqual = false;
		}
		return allEqual;
	}
	


	
	

}

