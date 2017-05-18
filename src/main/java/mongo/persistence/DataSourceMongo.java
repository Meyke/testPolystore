package mongo.persistence;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class DataSourceMongo {
	static final private String properties = "mongodb.properties";


	public DB getDatabase() throws UnknownHostException{
		DB db = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();  
        try (InputStream resourceStream = loader.getResourceAsStream(properties)) {
            props.load(resourceStream);
            int port = Integer.parseInt(props.getProperty("DB_PORT"));
            MongoClient mongoClient = new MongoClient( props.getProperty("DB_HOST"), port);
            db = mongoClient.getDB( props.getProperty("DB_NAME") );
        } catch (Exception e) {
        	e.printStackTrace();
        }            
		
		return db;
	}

}
