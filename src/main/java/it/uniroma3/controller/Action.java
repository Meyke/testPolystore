package it.uniroma3.controller;


//import it.uniroma3.model.QueryFacade;

import java.sql.ResultSet;
import java.util.List;

import it.uniroma3.JsonUtils.Convertitore;
import it.uniroma3.JsonUtils.SpezzatoreQuery;
import it.uniroma3.model.Facade;

import javax.servlet.http.HttpServletRequest;



//per adesso la nosta action funge da WorkFlowManager
public class Action {

	public String execute(HttpServletRequest request) throws Exception {
		String querySQL = request.getParameter("querySQL");
		String queryMongoDB = request.getParameter("queryMongoDB");
		String queryCypher = request.getParameter("queryCypher");
		Facade facade = new Facade();

		if(!querySQL.isEmpty()){
			SpezzatoreQuery spezzatoreQuery = new SpezzatoreQuery();
			spezzatoreQuery.spezza(querySQL);
			//-----
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



