package TestTiroc.TestTiroc;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.JSQLParserException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FabbricatoreMappaStatement {
	
	public void crea(){
		
	}
	public static void main(String[] args) throws UnknownHostException, FileNotFoundException, JSQLParserException {
		String stringaSql =  "SELECT persona.nome " +
                             "FROM persona , scuola "+
	                         "WHERE persona.scuola=scuola.id AND scuola.nome='caffe'";
	    Map<String, List<List<String>>> mappaWhere = new HashMap<>();
		SpezzatoreQuery spezzatore = new SpezzatoreQuery();
	    spezzatore.spezza(stringaSql);
	    List<List<String>> matriceWhere = spezzatore.getMatriceWhere();
		CaricatoreJSON caricatore = new CaricatoreJSON();
	    caricatore.caricaJSON();
	    JsonObject myjson = new JsonObject();
		Set<String> tabelle = caricatore.getJsonCheMiServono().keySet();
		Map<String, JsonObject> jsonUtili = caricatore.getJsonCheMiServono();
		
		
		
		for (String s : tabelle){
			List<List<String>> matriceWherePreciso = new LinkedList<>();
			myjson = jsonUtili.get(s);
			JsonArray attributi = myjson.getAsJsonArray("members");
			for(int i=0; i<attributi.size(); i++){
				String attributo = attributi.get(i).getAsString();	
				for(List<String> rigaMatriceWhere : matriceWhere){
					if (attributo.equals(rigaMatriceWhere.get(0)))
						matriceWherePreciso.add(rigaMatriceWhere);
				}
			}
			mappaWhere.put(s, matriceWherePreciso);
		}
		System.out.println(mappaWhere.toString());
	}

}
