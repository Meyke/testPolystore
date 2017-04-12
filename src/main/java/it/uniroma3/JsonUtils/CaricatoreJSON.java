package it.uniroma3.JsonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Questa classe serve a scaricare dei jsonObject che contengono informazioni su quelle particolari tabelle
 * che andrò a interrogare. 
 * @author micheletedesco1
 *
 */
public class CaricatoreJSON {
	private Map<String,JsonObject> jsonCheMiServono;
	private String tabellaPrioritaAlta;

	public CaricatoreJSON() {
		this.jsonCheMiServono = new HashMap<>();
	}
	
	/**
	 * @param collezioneTabelle una collezione contenente i nomi delle tabelle che andrò a interrogare
	 */
	public void caricaJSON(List<String> listaFrom) throws FileNotFoundException{
		File fileJSON = new File("/Users/micheletedesco1/Desktop/fileJSON.txt");
		Scanner scanner = new Scanner(fileJSON);
		double maxPriority = 10000;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			for(String tabella : listaFrom){			
				JsonParser parser = new JsonParser();
				JsonObject myJson = parser.parse(line).getAsJsonObject();
				String table = myJson.get("table").getAsString();
				if(table.equals(tabella)){
					double priority = myJson.get("priority").getAsDouble();
					if (priority <= maxPriority){
						maxPriority = priority;
						this.tabellaPrioritaAlta = tabella;
					}
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

	public String getTabellaPrioritaAlta() {
		return tabellaPrioritaAlta;
	}

	public void setTabellaPrioritaAlta(String tabellaPrioritaAlta) {
		this.tabellaPrioritaAlta = tabellaPrioritaAlta;
	}

	
	

}



