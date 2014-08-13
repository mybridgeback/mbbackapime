package co.mybridge;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.BasicBSONObject;
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
		MongoURI mongoURI = new MongoURI(System.getenv("MONGOHQ_URL"));
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
	 * this method is to take a person JSONObject, and convert it to BasicDBObject
	 * if email and password exist, then this is a registered user, otherwise, it's a leader object.
	 * @param p 
	 * @return  BasicDBObject to be saved into MongoDB, or return null if the JSONObject is not a person
	 */
    protected static BasicDBObject convertPersonToDBObject(JSONObject p) {
    	BasicDBObject retObj = new BasicDBObject();
    	try {
    		if (p.has("_id")) {
    			retObj.append("_id", p.get("_id"));
    		}
    		String pid = p.getString("person_id");
    		String fname = p.getString("fullname");
    		retObj.append("person_id", pid);
    		retObj.append("fullname", fname);
    		
    		JSONArray ind = p.getJSONArray("industries"); 	
    		ArrayList<String> indL = new ArrayList<String>();
    		for (int i = 0; i< ind.length(); i++) {
    			// verify string
    			String x = ind.getString(i);
    			indL.add(x);
    		}
    		retObj.append("industries", indL);
    		
    		JSONArray prof = p.getJSONArray("professions");
    		ArrayList<String> profL = new ArrayList<String>();
    		for (int j = 0; j< ind.length(); j++) {
    			// verify string
    			String x = prof.getString(j);
    			profL.add(x);
    		}
    		retObj.append("professions", profL);
    		
    		if (p.has("email") && p.has("password")) {
    			retObj.append("email", p.get("email"));
    			retObj.append("password", p.get("password"));
    		}
    		return retObj;
    	} catch(Exception x) {
    		System.out.println("Failed to recognize a person JSONObject: " + x.getMessage());
    		if (p != null) {
    			System.out.println("JSONObject: " + p.toString(4));
    		}
    		return null;
    	}
    }
    /**
     * Convert a person BasicDBObject to JSONObject. 
     * @param p
     * @return   JSONArray representing a person, or null in all other cases.
     */
    protected static JSONObject convertPersonToJSONObject(BasicDBObject p) {
    	JSONObject retObj = new JSONObject();
    	try {
    		if (p.containsField("_id")) {
    			retObj.append("_id", p.getString("_id"));
    		}
    		String pid = p.getString("person_id");
    		String fname = p.getString("fullname");
    		retObj.append("person_id", pid);
    		retObj.append("fullname", fname);
    		
    		List<String> indL = (List<String>)p.get("industries");
    		JSONArray indA = new JSONArray();  		
    		indA.put(indL);
    		retObj.append("industries", indA);
    		
    		List<String> profL = (List<String>)p.get("professions");
    		JSONArray profA = new JSONArray();
    		profA.put(profL);
    		retObj.append("professions", profA);
    		
    		if (p.containsField("email") && p.containsField("password")) {
    			retObj.put("email", p.getString("email"));
    			retObj.append("password", p.getString("password"));
    		}
    		return retObj;
    	} catch(Exception x) {
    		System.out.println("Failed to recognize a person from BasicDBObject: " + x.getMessage());
    		if (p != null) {
    			System.out.println("BasicDBObject: " + p.toString());
    		}
    		return null;
    	}
    }
    
    public static void addPerson(JSONObject p) throws MongoException, UnknownHostException  {
    	BasicDBObject pobj = convertPersonToDBObject(p);
    	if (pobj == null) {
    		return;
    	}
    	
    	DB  db = getMongoDB();
    	DBCollection coll = db.getCollection("mb_person");
    	coll.insert(pobj, WriteConcern.JOURNAL_SAFE);
    }
    
    public static JSONArray retrieveObjects(String collname, String... srchField) throws MongoException, UnknownHostException {
    	JSONArray retUsers = new JSONArray();
    	
    	DB  db = getMongoDB();
    	DBCollection coll = db.getCollection(collname);
    	DBCursor dbC = coll.find();
    	while (dbC.hasNext()) {
    		BasicDBObject dbo = (BasicDBObject)dbC.next();
    		JSONObject u = convertPersonToJSONObject(dbo);
    		retUsers.put(u);
    	}
    	return retUsers;
    }
}
