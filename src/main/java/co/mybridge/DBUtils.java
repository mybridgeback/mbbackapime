package co.mybridge;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;
import org.json.*;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;
import com.mongodb.WriteConcern;

public class DBUtils {

	public static DB getMongoDB() throws MongoException, UnknownHostException {
		String mongourl = System.getenv("MONGOHQ_URL");
		if (mongourl == null || mongourl.length() < 2) {
			mongourl = "mongodb://mb_user:mb_passwd@kahana.mongohq.com:10051/mongo_4_heroku";
		}
		MongoURI mongoURI = new MongoURI(mongourl);
		DB db = mongoURI.connectDB();
		db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());

		// Use the db object to talk to MongoDB
		Set<String> colls = db.getCollectionNames();
		for (String cname : colls) {
			System.out.println("collection =" + cname);
		}
		return db;
	}

    /**
     * generic retrieve method, with collection name and parameters
     * 
     * @param collname
     * @param srchField
     * @return  JSONArray with specified JSONObject
     * @throws MongoException
     * @throws UnknownHostException
     */
    public static JSONArray retrieveObjects(String collname, MBConverter conv, String... srchField) throws MongoException, UnknownHostException {
    	JSONArray retPeople = new JSONArray();
    	
    	DB  db = getMongoDB();
    	DBCollection coll = db.getCollection(collname);
    	DBCursor dbC = null;
    	if (srchField.length > 1) {
    		BasicDBObject srchobj = new BasicDBObject();
    		int l = srchField.length;
    		for (int j = 0; j< l-1; j=j+2) {
    			String f = srchField[j];
    			String v = srchField[j+1];
    			if (f.equalsIgnoreCase("_id")) {
    				srchobj.put(f, new ObjectId(v));
    			} else {
    				srchobj.append(f, v);
    			}
    		}
    		dbC = coll.find( srchobj );
    	} else {
    		dbC = coll.find();
    	}
    	while (dbC.hasNext()) {
    		BasicDBObject dbo = (BasicDBObject)dbC.next();
    		JSONObject jobj = conv.convertBasicDBToJSON(dbo);
    		retPeople.put(jobj);
    	}
    	return retPeople;
    }
}
