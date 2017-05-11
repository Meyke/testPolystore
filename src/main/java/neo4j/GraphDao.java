package neo4j;


import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;


/**
 * Questa classe si occupa di interrogare il database Neo4j, inviando una query Cypher
 * @author micheletedesco1
 *
 */
public class GraphDao {
	private DataSourceNeo4j datasource;
	private Session session;
	
	public GraphDao(){
		this.datasource = new DataSourceNeo4j();
		this.session = datasource.getSession();
	}
	
	public StatementResult interroga(String queryCQL){
		StatementResult result = session.run(queryCQL);
        return result;
	}
	
	public void chiudiConnessione(){
		this.datasource.chiudiConnesione();
	}
	

}
