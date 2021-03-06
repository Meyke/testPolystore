package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import net.sf.jsqlparser.JSQLParserException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Parser Cypher. Data la query scritta in Cypher, la scansiona (guardando le parole chiave, es: RETURN, MATCH) e permette di ottenere
 * l'insieme delle tabelle, l'insieme delle proiezioni, e l'insieme delle condizioni
 * @author micheletedesco1
 *
 */

public class ParserNeo4j {
	private List<String> listaProiezioni;
	private List<String> tableList;
	private List<List<String>> matriceWhere;


	public void spezza(String cypherQuery) throws JSQLParserException, FileNotFoundException{	
		//creo la lista from
		this.tableList = new LinkedList<>(); 
		ClassLoader classLoader = getClass().getClassLoader();
		File fileJSON = new File(classLoader.getResource("fileJSON.txt").getFile());
		Scanner scanner = new Scanner(fileJSON);
		//{'table' : 'persona', 'database' : 'postgerSQL', 'members':['persona.id', 'persona.nome', 'persona.scuola'] 'query' : 'SELECT * FROM persona WHERE 1=1'}
		//{'table' : 'scuola', 'database' : 'mongoDB', 'members':['scuola.id', 'scuola.nome'] }
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			JsonParser parser = new JsonParser();
			JsonObject myJson = parser.parse(line).getAsJsonObject();
			String table = myJson.get("table").getAsString();
			if (cypherQuery.toLowerCase().contains(table.toLowerCase()))
				this.tableList.add(table);
		}

		scanner.close();
		//System.out.println(tableList.toString());

		String[] parti2 = null;
		//creo la listaWhere
		this.matriceWhere = new LinkedList<>();
		String[] parti = cypherQuery.split(" WHERE ");
		if (parti.length>1){
			parti2 = parti[1].split(" RETURN ");

			String oggettoStringaWhere = parti2[0];
			String[] oggettiStatement = oggettoStringaWhere.split(" AND ");
			for (int i=0; i<oggettiStatement.length; i++){
				String[] oggettiStatementSeparati = null;
				String operation = null;
				if(oggettiStatement[i].contains("=")){
					oggettiStatementSeparati = oggettiStatement[i].split("=");
					operation = "=";
				}
				else if (oggettiStatement[i].contains("<")){
					oggettiStatementSeparati = oggettiStatement[i].split("<");
					operation = "<";
				}
				else {
					oggettiStatementSeparati = oggettiStatement[i].split(">");
					operation = ">";		
				}	
				List<String> rigaMatrice = new LinkedList<>();
				rigaMatrice.add(oggettiStatementSeparati[0].replaceAll("\\s+","")); //st = st.replaceAll("\\s+","")
				oggettiStatementSeparati[1] = oggettiStatementSeparati[1].replaceFirst("\\s+","");
				if (oggettiStatementSeparati[1].endsWith(" ")){
					oggettiStatementSeparati[1] = oggettiStatementSeparati[1].substring(0,oggettiStatementSeparati[1].length() - 1);
				}

				rigaMatrice.add(oggettiStatementSeparati[1]);
				rigaMatrice.add(operation);
				this.matriceWhere.add(rigaMatrice);			 		
			}
		}



		//creo la listaSelect
		this.listaProiezioni = new LinkedList<>();
		if (parti.length==1){
			parti2 = cypherQuery.split(" RETURN ");
		}	
		String oggettoStringaReturn = parti2[1];
		String[] partiReturn = oggettoStringaReturn.split("\\,");
		for (int i=0; i<partiReturn.length; i++){
			String elemento = partiReturn[i].replaceAll("\\s+","");
			this.listaProiezioni.add(elemento);
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

	public static void main(String[] args) throws FileNotFoundException, JSQLParserException {
		String cypherQuery ="MATCH (persona:persona), (scuola:scuola) WHERE persona.scuola=scuola.id AND scuola.nome='caffe' RETURN persona.name";
		ParserNeo4j parserNeo4j = new ParserNeo4j();
		parserNeo4j.spezza(cypherQuery);
		System.out.println("lista proiezioni----->" + parserNeo4j.getListaProiezioni().toString());
		System.out.println("lista tabelle----->" + parserNeo4j.getTableList().toString());
		System.out.println("lista clausule where [attributo valore]---->" + parserNeo4j.getMatriceWhere().toString());
	}
}

