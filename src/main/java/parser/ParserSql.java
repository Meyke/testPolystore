package parser;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
/**
 * Parser SQL (usando JSQLParser). Data la query scritta in SQL, la scansiona e permette di ottenere
 * l'insieme delle tabelle, l'insieme delle proiezioni, e l'insieme delle condizioni
 * @author micheletedesco1
 *
 */

public class ParserSql {
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
		Expression oggettoWhere = ps.getWhere();
		if (oggettoWhere != null){
			String oggettoStringaWhere = ps.getWhere().toString();
			String[] oggettiStatement = oggettoStringaWhere.split("AND");
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

	/*
	public static void main (String[] args) throws JSQLParserException{
		String stringaSql =  "SELECT customer.first_name, payment.amount FROM customer, address, payment WHERE customer.address_id = address.address_id and payment.customer_id = customer.customer_id and payment.amount > 20";
		ParserSql parser = new ParserSql();
		parser.spezza(stringaSql);
		System.out.println(parser.getMatriceWhere().toString());

	}
	*/

}
