package costruttoreQuery;


import java.util.List;
import java.util.Map;

import utility.Convertitore;
import utility.GestoreRisultato;
import mongo.MongoDao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
// ricordarsi di aprire il server ./mongod
/**
 * La classe ha il compito di creare una query Mongo prendendo come spunto le informazioni sulla tabella di
 * MongoDB (documento) da interrogare, le condizioni associate a quella tabella e i risultati di query precedenti
 * @author micheletedesco1
 *
 */
public class CostruttoreQueryMongo implements CostruttoreQuery {

	/**
	 * Il metodo serve a costruire la query Cypher.
	 * @param myJson un oggetto json che contiene informazioni su quella tabella (prelevate da un file per ora)
	 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
	 * @param mappaWhere una mappa con key = tabella e value = condizioni per quella tabella
	 * @param tabellaKnows contiene informazioni sulla tabella di join (se presente)
	 * @param jsonUtili contiene informazioni sulle tabelle interrogare
	 */	
	@Override
	public JsonArray eseguiQuery(JsonObject myJson, JsonArray risQueryPrec, Map<String, List<List<String>>> mappaWhere, JsonObject tabellaKnows, Map<String, JsonObject> jsonUtili) throws Exception {
		boolean richiestaJoin = false;
		String parametroJoin = null;
		String valueJoin = null;
		JsonArray risultato = null;
		String tabella = myJson.get("table").getAsString();
		String tabellaDaUnire = null;
		System.out.println(tabella);
		List<List<String>> condizioniPerQuellaTabella = mappaWhere.get(tabella);
		System.out.println(condizioniPerQuellaTabella.toString());
		
		
		BasicDBObject query = new BasicDBObject();
		System.out.println(condizioniPerQuellaTabella.size());
		if(condizioniPerQuellaTabella.size()==0)		
			risultato = eseguiQueryDirettamente(query,tabella);
		//può anche non sservire l'else
		for(int i=0; i<condizioniPerQuellaTabella.size(); i++){
			//estraggo la riga i-esima della matrice
			List<String> condizione = condizioniPerQuellaTabella.get(i);
			
			System.out.println(condizione.get(0));
			//effettuo un controllo per vedere se quella è una riga di join o meno
			if(!condizione.get(0).equals(tabellaKnows.get("foreignkey").getAsString())){
				String parametro = condizione.get(0).replace(tabella+".", "");
				System.out.println("parametro: "+ parametro);
				String valore= condizione.get(1).replaceAll("'", "");
				//vedo se il valore è un numero o una stringa (mongoDB è poco intelligente sui tipi)
				Object value = null;
				if (Character.isDigit(valore.charAt(0)))
					 value = Integer.parseInt(valore);
				else value = valore;
				System.out.println("valore: "+ value);
				query.put(parametro, value);
			}
			else{
				tabellaDaUnire = tabellaKnows.get("table").getAsString();
				richiestaJoin = true; 
				parametroJoin = condizione.get(0);
				valueJoin = condizione.get(1);
			}
			if (richiestaJoin)
				risultato = effettuaJoin(query, risQueryPrec, parametroJoin, valueJoin, tabella, tabellaDaUnire, jsonUtili);
			else{
				risultato = eseguiQueryDirettamente(query,tabella);
				System.out.println(risultato.toString());
			}
		}
		return risultato;
	}

	
	/**Questo metodo serve ad eseguire la query Mongo direttamente, nel caso non contenga join
	 * @param queryRiscritta
	 * @param tabella
	 */
	private JsonArray eseguiQueryDirettamente(DBObject queryRiscritta, String tabella) throws Exception { 
		MongoDao dao = new MongoDao();
		DBCursor cursor = dao.interroga(queryRiscritta,tabella);
		JsonArray documenti = new JsonArray();
		JsonParser parser = new JsonParser();
		while (cursor.hasNext()){
			BasicDBObject oggetto = (BasicDBObject) cursor.next();
			oggetto.removeField("_id");
			String documentoInFormatoStringa = oggetto.toString();
			JsonObject documentoInFormatoJson = parser.parse(documentoInFormatoStringa).getAsJsonObject();	
			documenti.add(documentoInFormatoJson);
		}
		documenti = Convertitore.convertMongoToJSON(documenti, tabella);
		return documenti;
	}

	/**
	 * Il metodo serve a effettuare il join, ovvero legare i risultati della query precedente alla query in corso di sviluppo
	 * @param queryRiscritta
	 * @param risQueryPrec un jsonArray che contiene i risultati della query precedente (per fare i join)
	 * @param parametroJoin la foreign key della tabella attuale (es. customer.store_id)
	 * @param valueJoin il valore della foreign key della tabella attuale (es. store.store_id)
	 * @param tabella nome della tabella da interrogare
	 * @param tabellaDaUnire nome della tabella eseguita in precedenza e da cui ho ottenuto i risultati
	 */
	private JsonArray effettuaJoin(DBObject query, JsonArray risQueryPrec, String parametroJoin, String valueJoin, String tabella, String tabellaDaUnire, Map<String, JsonObject> jsonUtili) throws Exception {
		String fk = parametroJoin;
		JsonObject elementoRisultatoPrecedente;
		JsonArray risultati = new JsonArray();
		
		for(int i=0; i<risQueryPrec.size(); i++){
			elementoRisultatoPrecedente = risQueryPrec.get(i).getAsJsonObject();
			System.out.println(elementoRisultatoPrecedente.toString());
			String parametro = valueJoin.split("\\.")[1];
			System.out.println("PARAMETRO="+parametro);
			System.out.println(elementoRisultatoPrecedente.toString());
			int valore;
			if(elementoRisultatoPrecedente.get(parametro) == null) //Dato che Neo4J restituisce dati del tipo per esempio: store.store_id
				valore =  elementoRisultatoPrecedente.get(valueJoin).getAsInt();
			else
				valore = elementoRisultatoPrecedente.get(parametro).getAsInt(); //casi senza Neo4J (es: store_id)
			DBObject queryTemporanea = new BasicDBObject();
			queryTemporanea.putAll(query);
			parametroJoin = parametroJoin.replace(tabella+".", "");
			queryTemporanea.put(parametroJoin, valore);
			System.out.println("query temporanea " + queryTemporanea.toString());
			JsonArray risultatiParziali = eseguiQueryDirettamente(queryTemporanea, tabella);
			risultati = concatArray(risultati, risultatiParziali);
			System.out.println(risultati.toString());
		}
		
		JsonArray risultatiUniti = GestoreRisultato.unisciColonne(risultati,risQueryPrec,fk, tabellaDaUnire, jsonUtili);
		return risultatiUniti;
	}		

	private JsonArray concatArray(JsonArray arr1, JsonArray arr2){
		JsonArray result = new JsonArray();
		for (int i = 0; i < arr1.size(); i++) {
			result.add(arr1.get(i));
		}
		for (int i = 0; i < arr2.size(); i++) {
			result.add(arr2.get(i));
		}
		return result;
	}
}
