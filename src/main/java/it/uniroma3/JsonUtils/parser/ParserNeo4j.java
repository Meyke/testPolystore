package it.uniroma3.JsonUtils.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import net.sf.jsqlparser.JSQLParserException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Il parser per query cypher.
 *
 */

//Non riuscivo a trovarne uno per java e me lo sono inventato
public class ParserNeo4j {
	private List<String> listaProiezioni;
	private List<String> tableList;
	private List<List<String>> matriceWhere;
	
	
	public void spezza(String cypherQuery) throws JSQLParserException, FileNotFoundException{	
		//creo la lista from
		this.tableList = new LinkedList<>(); 
		File fileJSON = new File("/Users/micheletedesco1/Desktop/fileJSON.txt");
		Scanner scanner = new Scanner(fileJSON);
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			JsonParser parser = new JsonParser();
			JsonObject myJson = parser.parse(line).getAsJsonObject();
			String table = myJson.get("table").getAsString();
			if (cypherQuery.toLowerCase().contains(table.toLowerCase()))
				this.tableList.add(table);
		}

		scanner.close();
		
		//creo la listaDiCondizioni
		this.matriceWhere = new LinkedList<>();
		String[] parti = cypherQuery.split(" WHERE ");
		String[] parti2 = null;
		if(parti.length==1)
			parti2 = parti[0].split(" RETURN ");
		else 
			parti2 = parti[1].split(" RETURN ");
		
		String oggettoStringaWhere = null;
		if(parti.length!=1){
			oggettoStringaWhere = parti2[0];
			String[] oggettiStatement = oggettoStringaWhere.split(" AND ");
			for (int i=0; i<oggettiStatement.length; i++){
				String[] oggettiStatementSeparati = oggettiStatement[i].split("=");
				List<String> rigaMatrice = new LinkedList<>();
				rigaMatrice.add(oggettiStatementSeparati[0]);
				rigaMatrice.add(oggettiStatementSeparati[1]);
				this.matriceWhere.add(rigaMatrice);			 		
			} 	
		}
    	//creo la listaDiProiezioni
    	this.listaProiezioni = new LinkedList<>();
    	String oggettoStringaReturn = parti2[1];
        String[] partiReturn = oggettoStringaReturn.split("\\,");
        for (int i=0; i<partiReturn.length; i++){
        	this.listaProiezioni.add(partiReturn[i]);
        }			
	}

	public List<String> getListaProiezioni() {
		return listaProiezioni;
	}

	public void setListaProiezioni(List<String> listaProiezioni) {
		this.listaProiezioni = listaProiezioni;
	}

	public List<String> getTableList() {
		return tableList;
	}

	public void setTableList(List<String> listaFrom) {
		this.tableList = listaFrom;
	}

	public List<List<String>> getMatriceWhere() {
		return matriceWhere;
	}

	public void setMatriceWhere(List<List<String>> matriceWhere) {
		this.matriceWhere = matriceWhere;
	}
}

