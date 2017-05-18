package controller;



import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.google.gson.reflect.TypeToken;




public class Action {
	private Set<String> attributi;
	private List<List<Object>> matriceRisultati;

	public String execute(HttpServletRequest request) throws Exception {
		EsecutoreThreads esecutore = new EsecutoreThreads();
		esecutore.avviaServer();
		ClientGUI clientGui = null;
		String risultatoStringa = null;
		String query = request.getParameter("query");
		try{
		clientGui = new ClientGUI();
		risultatoStringa = clientGui.call(query);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		      if (clientGui!= null) {
			        try {
			          clientGui.close();
			        }
			        catch (IOException _ignore) {} catch (Exception e) {
						e.printStackTrace();
					}
			      }
		      esecutore.stopServer();
		
		}
		if (risultatoStringa.equals("not query")){
			request.setAttribute("queryError", "query errata");
			return "/index.jsp";
		}
		JsonParser parser = new JsonParser();
		JsonArray risultato = parser.parse(risultatoStringa).getAsJsonArray();
		
		
		if(risultato.size()==0){
			request.setAttribute("queryError", "LA QUERY INSERITA NON HA PRODOTTO RISULTATO. ELEMENTO NON PRESENTE");
		}else{
			convertForPrinting(risultato);
			request.setAttribute("attributi", this.attributi);
			request.setAttribute("matriceRisultati", this.matriceRisultati);
			request.setAttribute("query", query);
			//return "/risultati.jsp";
		}
		
			return "/index.jsp";
		
	}

	private void convertForPrinting(JsonArray risultato) { //solo per poi stampare e far vedere i risultati
		this.matriceRisultati = new LinkedList<>();
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> riga = gson.fromJson(risultato.get(0), type);
		this.attributi = riga.keySet();
		for (int i=0; i<risultato.size();i++){
			Map<String, Object> rigaInFormatoMappa = gson.fromJson(risultato.get(i), type);
			List<Object> rigaRisultato = new LinkedList<>();
			for(String s:attributi){
				rigaRisultato.add(rigaInFormatoMappa.get(s));	
			}	
			this.matriceRisultati.add(rigaRisultato);
		}
		
	}

	




}




