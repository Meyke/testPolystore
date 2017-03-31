package TestTiroc.TestTiroc;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;



import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class ParserMongo {
	
	private List<String> listaProiezioni;
	private List<String> listaFrom;
	private List<List<String>> matriceWhere;
	
	
	public ParserMongo() {
		this.listaProiezioni = new LinkedList<>();
		this.listaFrom = new LinkedList<>();
		this.matriceWhere = new LinkedList<>();
	}
	

	public List<String> getListaProiezioni() {
		return listaProiezioni;
	}


	public void setListaProiezioni(List<String> listaProiezioni) {
		this.listaProiezioni = listaProiezioni;
	}


	public List<String> getListaFrom() {
		return listaFrom;
	}


	public void setListaFrom(List<String> listaFrom) {
		this.listaFrom = listaFrom;
	}


	public List<List<String>> getMatriceWhere() {
		return matriceWhere;
	}


	public void setMatriceWhere(List<List<String>> matriceWhere) {
		this.matriceWhere = matriceWhere;
	}


	public void spezza (String queryMongo) throws UnknownHostException{
		String tabella = this.dammiTabella(queryMongo);
		this.listaFrom.add(tabella);	
		String statement = this.dammiQuery(queryMongo);
		//controllare se statment isEmpty
		JsonParser parser = new JsonParser();
		JsonObject myJson = parser.parse(statement).getAsJsonObject();
		//List<String> documenti = new LinkedList<String>();
		System.out.println(myJson.toString());
		
		//trasformo il jsonObject in una mappa
		Map<String, Object> attributes = new HashMap<String, Object>();
        Set<Entry<String, JsonElement>> entrySet = myJson.entrySet();
        for(Map.Entry<String,JsonElement> entry : entrySet){
          attributes.put(entry.getKey(), myJson.get(entry.getKey()));
        }

        for(Map.Entry<String,Object> att : attributes.entrySet()){
            System.out.println("key >>> "+att.getKey());
            System.out.println("val >>> "+att.getValue());
            } 
        //trasformo la mappa in una listaWhere
        
        for(Map.Entry<String,Object> att : attributes.entrySet()){
        	List<String> rigaMatrice = new LinkedList<>();
        	rigaMatrice.add(att.getKey());
        	rigaMatrice.add(att.getValue().toString());
        	this.matriceWhere.add(rigaMatrice);	
        }
		
		
		
		
	}
	
	private String dammiTabella(String queryMongo){
		String[] parti = queryMongo.split("\\.");
		String tabella = parti[1];
		return tabella;
	}
	
	private String dammiQuery(String queryMongo){
		String[] parti = queryMongo.split("\\.",3);
		String parte3 = parti[2];
		parti =  parte3.split("\\(");
		String queryNONbuona = parti[1];
		String query = queryNONbuona.substring(0,queryNONbuona.length()-1);
		return query;
	}
	
	public static void main(String[] args) throws UnknownHostException {
		String queryMongo ="db.persona.find({persona.nome:'michele', persona.cognome:'tedesco'})"; 
		ParserMongo parserMongo = new ParserMongo();
		parserMongo.spezza(queryMongo);
		//System.out.println("lista proiezioni----->" + parserNeo4j.getListaProiezioni().toString());
		System.out.println("lista tabelle----->" + parserMongo.getListaFrom().toString());
		System.out.println("lista clausule where [attributo valore]---->" + parserMongo.getMatriceWhere().toString());
	}

}
