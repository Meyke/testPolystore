package TestTiroc.TestTiroc;

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
 * è il parser per query sql
 *
 */
public class SpezzatoreQuery { 

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
    			String[] oggettiStatementSeparati = oggettiStatement[i].split("=");
    			List<String> rigaMatrice = new LinkedList<>();
    			rigaMatrice.add(oggettiStatementSeparati[0].replaceAll("\\s+","")); //st = st.replaceAll("\\s+","")
    			rigaMatrice.add(oggettiStatementSeparati[1].replaceAll("\\s+",""));
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
	public static void main( String[] args ) throws JSQLParserException
    {
		String stringaSql =  "SELECT customer.last_Name " +
                             "FROM customer , rental , inventory "+
				             "WHERE customer.customer_ID=rental.customer_ID AND rental.inventory_ID=inventory.inventory_id AND inventory.film='Titanic'";
		
		SpezzatoreQuery spezzatore = new SpezzatoreQuery();
		spezzatore.spezza(stringaSql);
		System.out.println("lista proiezioni----->" + spezzatore.getListaProiezioni().toString());
		System.out.println("lista tabelle----->" + spezzatore.getTableList().toString());
		System.out.println("lista clausule where [attributo valore]---->" + spezzatore.getMatriceWhere().toString());
		
		String prova = "customer";
		System.out.println(prova.equals(spezzatore.getTableList().get(0)));
		prova = "customer.customer_ID";
		System.out.println(prova.equals(spezzatore.getMatriceWhere().get(0).get(0)));
		
		System.out.println("\n\n\n");
		
		String stringaSql2 =  "SELECT customer.last_Name " +
                              "FROM customer";
		spezzatore.spezza(stringaSql2);
		System.out.println("lista proiezioni----->" + spezzatore.getListaProiezioni().toString());
		System.out.println("lista tabelle----->" + spezzatore.getTableList().toString());
		System.out.println("lista clausule where [attributo, valore]---->" + spezzatore.getMatriceWhere().toString());
        
		
    
    }
}
