package parser;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;






import utility.CaricatoreJSON;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
/**
 * Parser Mongo (usando JSQLParser). Data la query scritta in Mongo, la scansiona e permette di ottenere
 * l'insieme delle tabelle, l'insieme delle proiezioni, e l'insieme delle condizioni
 * @author micheletedesco1
 *
 */
public class ParserMongov2 {

	private List<String> tabelle;
	private List<String> listaProiezioni;
	private List<List<String>> matriceWhere;


	public ParserMongov2() {
		this.tabelle = new LinkedList<>();
		this.matriceWhere = new LinkedList<>();
		this.listaProiezioni = new LinkedList<>();
	}


	public List<String> getTabelle() {
		return tabelle;
	}


	public void setTabelle(List<String> tabelle) {
		this.tabelle = tabelle;
	}


	public List<String> getListaProiezioni() {
		return listaProiezioni;
	}


	public void setListaProiezioni(List<String> colonneProiezioni) {
		this.listaProiezioni = colonneProiezioni;
	}


	public List<String> getListaFrom() {
		return tabelle;
	}


	public void setListaFrom(List<String> listaFrom) {
		this.tabelle = listaFrom;
	}


	public List<List<String>> getMatriceWhere() {
		List<List<String>> nuovaMatriceWhere = new LinkedList<>();
		for (List<String> condizione : this.matriceWhere){ //per sostituire le "" con '', altrimenti non funziona;
			String elemento = condizione.get(1);
			char c = elemento.charAt(0);
			if(c=='"'){
				elemento = elemento.substring(1, elemento.length()-1);
				elemento = "'"+elemento+"'";
			}
			condizione.set(1, elemento);
			nuovaMatriceWhere.add(condizione);
		}
		matriceWhere = nuovaMatriceWhere;
		for (List<String> condizione : this.matriceWhere){
			condizione.add("=");
		}
		return matriceWhere;
	}


	public void setMatriceWhere(List<List<String>> matriceWhere) {
		this.matriceWhere = matriceWhere;
	}


	public void spezza (String queryMongo) throws UnknownHostException, FileNotFoundException{
		String tabella = this.dammiTabella(queryMongo);
		this.tabelle.add(tabella);	
		String statement = this.dammiQuery(queryMongo);
		//controllare se statment isEmpty
		JsonParser parser = new JsonParser();
		JsonArray myJsonArray = parser.parse(statement).getAsJsonArray();
		JsonObject myJson = myJsonArray.get(0).getAsJsonObject();
		if(myJsonArray.size()>1){
			JsonObject colonneDaSelezionare = myJsonArray.get(1).getAsJsonObject();
			aggiornaColonneProiezioni(colonneDaSelezionare);
		}

		aggiornamentoTabelleECondizioni(this.matriceWhere, this.tabelle.get(0), myJson);	


	}

	private void aggiornaColonneProiezioni(JsonObject colonneDaSelezionare) {
		Set<Entry<String, JsonElement>> entrySet = colonneDaSelezionare.entrySet();
		for(Map.Entry<String,JsonElement> entry : entrySet){
			this.listaProiezioni.add(entry.getKey());
		}

	}


	private void aggiornamentoTabelleECondizioni(List<List<String>> matriceWhere, String tabella, JsonObject myJson) throws FileNotFoundException {

		//trasformo il jsonObject in una mappa
		Map<String, Object> attributes = new HashMap<String, Object>();
		Set<Entry<String, JsonElement>> entrySet = myJson.entrySet();
		for(Map.Entry<String,JsonElement> entry : entrySet){
			attributes.put(entry.getKey(), myJson.get(entry.getKey()));
		}

		for(Map.Entry<String,Object> att : attributes.entrySet()){
			List<String> rigaMatrice = new LinkedList<>();
			rigaMatrice.add(att.getKey());
			rigaMatrice.add(att.getValue().toString());
			this.matriceWhere.add(rigaMatrice);	
		}

		//System.out.println("OGGETTO DA STUDIARE: " +myJson);
		//serve solo per il metodo di dopo (caricaJSON di caricatore) e non caricare tutti i json
		List<String> tabelleAppoggio = new LinkedList<>();
		tabelleAppoggio.add(tabella);
		JsonObject nuovoJson = null;
		//System.out.println(tabella);
		CaricatoreJSON caricatoreDaFile = new CaricatoreJSON();
		caricatoreDaFile.caricaJSON(tabelleAppoggio);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDaFile.getJsonCheMiServono();
		JsonObject tabellaInformazioni = jsonUtili.get(tabella);
		//System.out.println(jsonUtili.toString());
		JsonArray tabelleCheConosce = tabellaInformazioni.get("knows").getAsJsonArray();
		for (int i=0; i<tabelleCheConosce.size();i++){
			String tabellaCheConosce = tabelleCheConosce.get(i).getAsJsonObject().get("table").getAsString();
			String foreignKey = tabelleCheConosce.get(i).getAsJsonObject().get("foreignkey").getAsString();
			List<List<String>> matriceWhereCopia = new LinkedList<>(matriceWhere);
			for(List<String> condizione : matriceWhereCopia){
				List<String> nuovaCondizione = new LinkedList<>();
				//System.out.println("TABELLA CHE CONOSCE: "+tabellaCheConosce);
				//System.out.println("CONDIZIONE: "+condizione.get(0));
				if((condizione.get(0).equals(tabellaCheConosce)) && myJson.get(tabellaCheConosce)!=null){ //se ho dei join, saranno espressi in mongo come jsonObject
					rimuoviElemento(tabellaCheConosce, this.matriceWhere);
					//System.out.println("MYJSON: " + myJson.toString());
					nuovoJson = myJson.get(tabellaCheConosce).getAsJsonObject();
					this.tabelle.add(tabellaCheConosce);
					caricatoreDaFile.caricaJSON(this.tabelle);
					jsonUtili = caricatoreDaFile.getJsonCheMiServono();
					String pkTabellaCheConosce = jsonUtili.get(tabellaCheConosce).get("primarykey").getAsString();
					nuovaCondizione.add(foreignKey);
					nuovaCondizione.add(pkTabellaCheConosce);
					this.matriceWhere.add(nuovaCondizione);
					aggiornamentoTabelleECondizioni(this.matriceWhere, tabellaCheConosce, nuovoJson);
				}
			}
		}


	}




	private void rimuoviElemento(String tabellaCheConosce, List<List<String>> matriceWhere2) {
		int i=0;
		int iteratore=0;
		for (List<String> condizione: matriceWhere2){
			if(condizione.get(0).equals(tabellaCheConosce))
				iteratore = i;
			i++;
		}
		//System.out.println("ITERATORE: "+iteratore);
		//System.out.println("MATRICE WHERE: "+matriceWhere2.toString());
		this.matriceWhere.remove(iteratore);

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
		String query = queryNONbuona.replaceAll("\\)", "]");
		query = "[" + query;
		return query;
	}

	public static void main(String[] args) throws UnknownHostException, FileNotFoundException {
		//String queryMongo ="db.customer.find({customer.first_name:'michele', customer.last_name:'tedesco', address:{address.nome:'via sala'}})"; //OK 
		//String queryMongo ="db.customer.find({customer.first_name:'michele', customer.last_name:'tedesco'})"; //OK
		//String queryMongo ="db.customer.find({customer.first_name:'michele', customer.last_name:'tedesco', address:{city:{city.city_id:1}}})"; //OK

		String queryMongo ="db.customer.find({customer.first_name:'michele', customer.last_name:'tedesco', address:{address.nome:'via sala'}, store:{store.store_id:1}})"; //OK 
		//db.actor.find({first_name:'Arnold'},{first_name:true,_id:false})
		//String queryMongo ="db.customer.find({},{customer.first_name:true})"; //OK 
		//String queryMongo ="db.customer.find({})";

		ParserMongov2 parserMongo = new ParserMongov2();
		parserMongo.spezza(queryMongo);
		System.out.println("lista tabelle: " + parserMongo.getListaFrom().toString());
		System.out.println("lista clausule where [attributo valore]: " + parserMongo.getMatriceWhere().toString());
		System.out.println("colonne da mostrare: " + parserMongo.getListaProiezioni().toString());
	}

}
