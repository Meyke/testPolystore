package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
		//{'table' : 'persona', 'database' : 'postgerSQL', 'members':['persona.id', 'persona.nome', 'persona.scuola'] 'query' : 'SELECT * FROM persona WHERE 1=1'}
		//{'table' : 'scuola', 'database' : 'mongoDB', 'members':['scuola.id', 'scuola.nome'] }
		while (scanner.hasNextLine()) {
			for(String tabella : listaFrom){			
				String line = scanner.nextLine();
				JsonParser parser = new JsonParser();
				JsonObject myJson = parser.parse(line).getAsJsonObject();
				String table = myJson.get("table").getAsString();
				if(table.equals(tabella)){
					System.out.println(tabella);
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

	

}

