package TestTiroc.TestTiroc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
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

	public void caricaJSON() throws FileNotFoundException{//dovrei io passare la listafrom
		List<String> listaFrom = new LinkedList<>(); 
		//listaFrom.add("persona");
		listaFrom.add("scuola");
		listaFrom.add("indirizzo");

		File fileJSON = new File("/Users/micheletedesco1/Desktop/fileJSON.txt");
		Scanner scanner = new Scanner(fileJSON);
		//{'table' : 'persona', 'database' : 'postgerSQL', 'members':['persona.id', 'persona.nome', 'persona.scuola'] 'query' : 'SELECT * FROM persona WHERE 1=1'}
		//{'table' : 'scuola', 'database' : 'mongoDB', 'members':['scuola.id', 'scuola.nome'] }
		//{'table' : 'indirizzo', 'database' : 'neo4j', 'knows' : 'nessuno', 'members':['indirizzo.id', 'indirizzo.nome'], 'query' : 'MATCH (indirizzo:indirizzo) WHERE 1=1' , 'foreignkey' : 'blebleble'}
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			//System.out.println(line);
			for(String tabella : listaFrom){			
				//System.out.println(line);
				//System.out.println("------------------");
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

	public static void main(String[] args) throws FileNotFoundException {
		CaricatoreJSON caricatore = new CaricatoreJSON();
		caricatore.caricaJSON();
		System.out.println(caricatore.getJsonCheMiServono().toString()); 
	}

}

