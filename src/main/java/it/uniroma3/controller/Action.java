package it.uniroma3.controller;


//import it.uniroma3.model.QueryFacade;


import it.uniroma3.model.Facade;
import it.uniroma3.model.FacadeSQL;

import javax.servlet.http.HttpServletRequest;




//per adesso la nosta action funge da WorkFlowManager
public class Action {

	public String execute(HttpServletRequest request) throws Exception {
		String querySQL = request.getParameter("querySQL");
		//String queryMongoDB = request.getParameter("queryMongoDB");
		String queryCypher = request.getParameter("queryCypher");
		//String queryFORtable = request.getParameter("tabella");
		Facade facade = new Facade();

		if(!querySQL.isEmpty()){ //refactoring con classe specifica
			FacadeSQL facadeSQL = new FacadeSQL();
			String risultato = facadeSQL.gestisciQuery(querySQL);
			request.setAttribute("result", risultato);
			return "/index.jsp";
		}

		/*else {
			if (!queryMongoDB.isEmpty()){
				List<String> documenti = facade.interrogaMongoDB(queryMongoDB);
				request.setAttribute("resultMongo",documenti);
				return "/stampaMongoDB.jsp";
			}*/
			else{
				String risultatiNeo4j = facade.interrogaNeo4j(queryCypher);
				request.setAttribute("resultNeo4j",risultatiNeo4j);
				return "/stampaNeo4j.jsp";
			}
		}




	}




