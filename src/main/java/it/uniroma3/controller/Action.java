package it.uniroma3.controller;


//import it.uniroma3.model.QueryFacade;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import it.uniroma3.JsonUtils.CaricatoreJSON;
import it.uniroma3.JsonUtils.Convertitore;
import it.uniroma3.JsonUtils.FabbricatoreMappaStatement;
import it.uniroma3.JsonUtils.ParserSql;
import it.uniroma3.model.Facade;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;



//per adesso la nosta action funge da WorkFlowManager
public class Action {

	public String execute(HttpServletRequest request) throws Exception {
		String querySQL = request.getParameter("querySQL");
		String queryMongoDB = request.getParameter("queryMongoDB");
		String queryCypher = request.getParameter("queryCypher");
		Facade facade = new Facade();

		if(!querySQL.isEmpty()){ //refactoring con classe specifica
			ParserSql parser = new ParserSql();
			parser.spezza(querySQL);//spezzo la query
			List<String> tabelle = parser.getTableList();//ottengo le tabelle che formano la query
			List<List<String>> matriceWhere = parser.getMatriceWhere();
			CaricatoreJSON caricatoreDAFile = new CaricatoreJSON();
			caricatoreDAFile.caricaJSON(tabelle);//carico da file i json utili in base alle tabelle
			Map<String, JsonObject> jsonUtili = caricatoreDAFile.getJsonCheMiServono();
			FabbricatoreMappaStatement fabbricatoreCondizione = new FabbricatoreMappaStatement();
			fabbricatoreCondizione.creaMappaWhere(matriceWhere, jsonUtili);
			Map<String, List<List<String>>> mappaWhere = fabbricatoreCondizione.getMappaWhere();
			
			
			ResultSet result = facade.interrogaPostgres(querySQL);
			Convertitore convertitore = new Convertitore();
			String resultJSON = convertitore.convertResultSetIntoJSON(result);
			request.setAttribute("result", resultJSON);
			return "/stampaPostgres.jsp";
		}

		else {
			if (!queryMongoDB.isEmpty()){
				List<String> documenti = facade.interrogaMongoDB(queryMongoDB);
				request.setAttribute("resultMongo",documenti);
				return "/stampaMongoDB.jsp";
			}
			else{
				String risultatiNeo4j = facade.interrogaNeo4j(queryCypher);
				request.setAttribute("resultNeo4j",risultatiNeo4j);
				return "/stampaNeo4j.jsp";
			}
		}




	}
}



