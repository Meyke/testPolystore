package it.uniroma3.persistence.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.net.UnknownHostException;
import java.util.Set;

public class MongoDao {

	private Mongo connection = null;
	private DB db = null;

	private static MongoDao mongoDao = null;

	@SuppressWarnings("deprecation")
	public MongoDao() throws UnknownHostException{
		connection= new Mongo("mongodb://localhost:27020/test" ,27017);
		db = connection.getDB("test");
	}
	public static MongoDao getInstance() throws UnknownHostException{
		if(mongoDao==null){
			mongoDao = new MongoDao();
		}
		return mongoDao;
	}

	public void createTable(String tableName)throws Exception{
		Set<String> tableNames = db.getCollectionNames();
		if(!tableNames.contains(tableName)){
			DBObject dbobject = new BasicDBObject();
			db.createCollection(tableName, dbobject);
		}
	}

	public void saveToDB(String tableName, BasicDBObject dbObject)throws Exception{
		DBCollection dbCollection = db.getCollection(tableName);
		dbCollection.insert(dbObject);
	}

	public Set<String> getTableNames()throws Exception{
		return db.getCollectionNames();
	}

	public void showDB(String tableName)throws Exception{
		DBCollection dbCollection = db.getCollection(tableName);
		DBCursor cur = dbCollection.find();
		while(cur.hasNext()) {
			System.out.println(cur.next()); 
		}
	}

	public DBCursor getAllRows(String tableName)throws Exception{
		DBCollection dbCollection = db.getCollection(tableName);
		DBCursor cur = dbCollection.find();
		return cur;
	}

	public int getRowCount(String tableName)throws Exception{
		DBCollection dbCollection = db.getCollection(tableName);
		DBCursor cur = dbCollection.find();
		return cur.count();
	}

	public DBCursor findByColumn(String tableName, DBObject whereClause)throws Exception{
		DBCursor result = null;
		DBCollection dbCollection = db.getCollection(tableName);
		result = dbCollection.find(whereClause);
		return result;
	}

	public void createIndex(String tableName, String columnName) throws Exception{
		DBCollection dbCollection = db.getCollection(tableName);
		DBObject indexData = new BasicDBObject(columnName,1);
		dbCollection.createIndex(indexData);
	}

	public void dropTable(String collectionName){
		db.getCollection(collectionName).drop();
	}

}


