package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RelationalDao {
	private DataSource dataSource;

	public RelationalDao() {
		this.dataSource = new DataSource();
	}

	public ResultSet interroga(String querySQL) {
		Connection connection = this.dataSource.getConnection();
		ResultSet result;
		try {
			PreparedStatement statement;
			statement = connection.prepareStatement(querySQL);
			result = statement.executeQuery();
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}	
		return result;
	}

}
