package neo4j;


import java.io.InputStream;
import java.util.Properties;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;




/**
 * Classe che stabilisce una connessione al database Neo4j
 * @author micheletedesco1
 *
 */
public class DataSourceNeo4j {
	static final private String properties = "neo4j.properties";
	private Driver driver;
	private Session session;
	
	public Session getSession(){
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(properties)) {
            props.load(resourceStream);
            String uri = props.getProperty("DB_URL");
            String user = props.getProperty("DB_USER");
            String pass = props.getProperty("DB_PASS");
            this.driver = GraphDatabase.driver( uri, AuthTokens.basic( user, pass ) );
    		this.session = driver.session();
    		

        } catch (Exception e) {
        	e.printStackTrace();
        }
        return this.session;	
	}

	public void chiudiConnesione() {
		this.session.close();
		this.driver.close();

		
	}

}
