package test;

import java.sql.*;


//bisogna mettere anche qui il logger 
public class DataSource {
	
	
	private String dbURI = "jdbc:postgresql://localhost/testTir";
	private String userName = "postgres";
	private String password = "rasenshuriken";

	public Connection getConnection() throws PersistenceException {
		Connection connection = null;
		try {
		    Class.forName("org.postgresql.Driver");
		    connection = DriverManager.getConnection(dbURI,userName, password);
		} catch (ClassNotFoundException e) {
			throw new PersistenceException(e.getMessage());
		} catch(SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		return connection;
	}
}
