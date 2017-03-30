package it.uniroma3.JsonUtils;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

// testato e funzionante. attenzione ai valori stringa es film="titanic"
public class SpezzatoreQuery { //spezzatore query sql
	private List<String> tableList;
	private List<String> listaProiezioni;
	private List<List<String>> matriceWhere;
	
	public void spezza(String querySQL) throws JSQLParserException{
		
		//utilizzo il parser jsql
    	CCJSqlParserManager parserManager = new CCJSqlParserManager();
    	Select select = (Select) parserManager.parse(new StringReader(querySQL));
    	PlainSelect ps = (PlainSelect) select.getSelectBody();
    	
    	
    	//creo la listaFROM
    	TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    	this.tableList = tablesNamesFinder.getTableList(select);
    	
    	//creo la listaSELECT
    	List<SelectItem> listaSelectItems = ps.getSelectItems();
    	this.listaProiezioni = new LinkedList<>();
    	for (SelectItem elemento : listaSelectItems){
    		String elementoStringato = elemento.toString();
    		this.listaProiezioni.add(elementoStringato);
    	}
    	
    	//creo la matriceWHERE
    	this.matriceWhere = new LinkedList<>();
    	String oggettoStringaWhere = ps.getWhere().toString();
    	String[] oggettiStatement = oggettoStringaWhere.split("AND");
    	for (int i=0; i<oggettiStatement.length; i++){
    		String[] oggettiStatementSeparati = oggettiStatement[i].split("=");
    		List<String> rigaMatrice = new LinkedList<>();
    		rigaMatrice.add(oggettiStatementSeparati[0]);
    		rigaMatrice.add(oggettiStatementSeparati[1]);
    		this.matriceWhere.add(rigaMatrice);			 		
    	} 	
	}

	public List<String> getTableList() {
		return tableList;
	}

	public void setTableList(List<String> tableList) {
		this.tableList = tableList;
	}

	public List<String> getListaProiezioni() {
		return listaProiezioni;
	}

	public void setListaProiezioni(List<String> listaProiezioni) {
		this.listaProiezioni = listaProiezioni;
	}

	public List<List<String>> getMatriceWhere() {
		return matriceWhere;
	}

	public void setMatriceWhere(List<List<String>> matriceWhere) {
		this.matriceWhere = matriceWhere;
	}
	
	

}
