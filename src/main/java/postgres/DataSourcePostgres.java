package postgres;



import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Classe che stabilisce una connesione al database postgres
 * @author micheletedesco1
 *
 */
public class DataSourcePostgres {
	
	static final private String properties = "postgres.properties";



	public Connection getConnection() throws PersistenceException {
		Connection connection;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(properties)) {
            props.load(resourceStream);

            // load the Driver Class
            Class.forName(props.getProperty("DB_DRIVER_CLASS"));

            // create the connection now
            connection = DriverManager.getConnection(props.getProperty("DB_URL"),
                    props.getProperty("DB_USERNAME"),
                    props.getProperty("DB_PASSWORD"));
        } catch (IOException | ClassNotFoundException | SQLException e) {

            throw new PersistenceException(e.getMessage());
        }
        return connection;
	}
}
