package coseUtili;

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
public class CaricatoreJSON {
	private Map<String,JsonObject> jsonCheMiServono;
	public CaricatoreJSON() {
		this.jsonCheMiServono = new HashMap<>();
	}

	public void caricaJSON(List<String> listaFrom) throws FileNotFoundException{
		File fileJSON = new File("/Users/micheletedesco1/Desktop/fileJSON.txt");
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
//svolto cosi per risolvere dei bug
	public List<String> getTabellaPrioritaAlta(List<String> tabelle, Map<String, JsonObject> jsonUtili) {
		//String tabellaPreferita = tabelle.get(0);
		System.out.println(tabelle);
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
					if (tableKnows.get("table").getAsString().equals(tabella)){//vuol dire che la conosce
						String tabellaPreferita = oggetto.get("table").getAsString();
						tabellePreferite.add(tabellaPreferita);		
					}
				}
			}
		}	
		System.out.println(tabellePreferite);
		return tabellePreferite;
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

