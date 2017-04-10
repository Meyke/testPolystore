package it.uniroma3.model;



import it.uniroma3.JsonUtils.CaricatoreJSON;
import it.uniroma3.JsonUtils.FabbricatoreMappaStatement;
import it.uniroma3.JsonUtils.GestoreQuery;
import it.uniroma3.JsonUtils.ParserSql;

import java.util.List;
import java.util.Map;







import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FacadeSQL {
	
	public String gestisciQuery(String querySQL) throws Exception{
		ParserSql parser = new ParserSql();
		parser.spezza(querySQL);//spezzo la query
		List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
		List<List<String>> matriceWhere = parser.getMatriceWhere();
		CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
		caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
		Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
		JsonObject questoJson = jsonUtili.get("scuola");
		FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
		fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
		Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
		GestoreQuery gestoreQuerySql = new GestoreQuery();
		JsonArray risultato = gestoreQuerySql.esegui(questoJson, null, jsonUtili, mappaWhere);
		return risultato.toString();
	}
}
