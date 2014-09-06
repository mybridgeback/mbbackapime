package co.mybridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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

	private static DB mongoDB = null;
	
	public static synchronized DB getMongoDB() throws MongoException, UnknownHostException {
		
		if (mongoDB != null) return mongoDB;
		
		String mongourl = System.getenv("MONGOHQ_URL");
		if (mongourl == null || mongourl.length() < 2) {
			mongourl = "mongodb://mb_user:mb_passwd@kahana.mongohq.com:10051/mongo_4_heroku";
		}
		MongoURI mongoURI = new MongoURI(mongourl);
		mongoDB = mongoURI.connectDB();
		mongoDB.authenticate(mongoURI.getUsername(), mongoURI.getPassword());

		// Use the DB object to talk to MongoDB
		Set<String> colls = mongoDB.getCollectionNames();
		for (String cname : colls) {
			System.out.println("collection =" + cname);
		}
		return mongoDB;
	}

	/**
	 * create or update a MongoDB document
	 * @param collname
	 * @param conv
	 * @param jobj
	 * @return  a string representing ObjectID upon successful update, or null if failed.
	 */
	public static String updateObject(String collname, MBConverter conv, JSONObject jobj) {
    	BasicDBObject pobj = conv.convertJSONToBasicDB(jobj);
    	if (pobj == null) {
    		return null;
    	}
    	// add image info
    	if (jobj.has("thumbImage")) {
    		pobj.put("thumbImage", jobj.getString("thumbImage"));
    		if (jobj.has("thumbWidth") && jobj.has("thumbHeight")) {
    			// all set
    		} else {
    			// need to populate dimensions
    			addThumbImageDimensionFromURL(jobj, jobj.getString("thumbImage"));
    		}
    		pobj.put("thumbWidth", jobj.getInt("thumbWidth"));
			pobj.put("thumbHeight", jobj.getInt("thumbHeight"));
    	}
    	try {
	    	DB  db = getMongoDB();
	    	DBCollection coll = db.getCollection(collname);
	    	if (pobj.containsField("_id")) {
	    		// it is an update
	    		System.out.println("Updating an " + collname + " entry with: " + pobj.toString());
	    		BasicDBObject locateIt = new BasicDBObject();
	    		locateIt.put( "_id", pobj.getObjectId("_id") );
	    		
	    		BasicDBObject update = new BasicDBObject();
	    		update.put( "$set", pobj);

	    		coll.update( locateIt, pobj );
	    	} else {
	    		coll.insert(pobj, WriteConcern.JOURNAL_SAFE);
	    		System.out.println("Creating a " + collname +" entry: " + pobj.toString());
	    	}
	    	ObjectId id = pobj.getObjectId( "_id" );
	    	return id.toString();
    	}
    	catch(Exception x) {
    		// set to recreate mongoDB connection
    		mongoDB = null;
    		x.printStackTrace(System.out);
    		return null;
    	}
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
    public static JSONArray retrieveObjects(HttpServletRequest req, String collname, MBConverter conv, String... srchField) throws UnknownHostException {
    	JSONArray retObjects = new JSONArray();
    	
    	try {
	    	DB  db = getMongoDB();
	    	DBCollection coll = db.getCollection(collname);
	    	DBCursor dbC = null;
	    	if (srchField.length > 1) {
	    		BasicDBList idList = new BasicDBList();
	    		BasicDBList personList = new BasicDBList();
	    		BasicDBList profList = new BasicDBList();
	    		BasicDBList followerList = new BasicDBList();
	    		BasicDBList typeList = new BasicDBList();
	    		
	    		BasicDBObject srchobj = new BasicDBObject();
	    		int l = srchField.length;
	    		for (int j = 0; j< l-1; j=j+2) {
	    			String f = srchField[j];
	    			String v = srchField[j+1];
	    			if (f.equalsIgnoreCase("_id") ) {
	    				idList.add(new ObjectId(v));
	    			} else if ( f.equalsIgnoreCase("personId")){
	    				personList.add(v);
	    			} else if ( f.equalsIgnoreCase("profession")){
	    				profList.add(v);
	    			} else if ( f.equalsIgnoreCase("followerId")){
	    			    followerList.add(v);
	    			} else if ( f.equalsIgnoreCase("contentType")){
	    				typeList.add(v);
	    			}
	    			else {
	    				srchobj.put(f, v);
	    			}
	    		}
	    		if (idList.size() > 0) {
    				srchobj.append("_id", new BasicDBObject("$in", idList));
    			} 
	    		if (personList.size() > 0) {
	    			srchobj.append("personId", new BasicDBObject("$in", personList));
	    		}
	    		if (profList.size() > 0) {
	    			srchobj.append("professions", new BasicDBObject("$in", profList));
	    		}
	    		if (followerList.size() > 0) {
	    			srchobj.append("followerId", new BasicDBObject("$in", followerList));
	    		}
	    		if (typeList.size() > 0) {
	    			srchobj.append("contentType", new BasicDBObject("$in", typeList));
	    		}
	    		dbC = coll.find( srchobj );
	    	} else {
	    		dbC = coll.find();
	    	}
	    	while (dbC.hasNext()) {
	    		BasicDBObject dbo = (BasicDBObject)dbC.next();
	    		JSONObject jobj = conv.convertBasicDBToJSON(dbo);	
	    		// common code
	    		// add entityThumb when file exist
	    		if (dbo.containsField("thumbImage") && dbo.getString("thumbImage").length() > 4) {
	    		    copyImageDimensions(dbo, jobj);
	    		}
	    		retObjects.put(jobj);
	    	}
    	}
    	catch(MongoException e) {
    		mongoDB = null;
    		System.out.println("Failed to retrieve objects from MongoDB");
    		e.printStackTrace();
    	}
    	return retObjects;
    }
    
    static protected JSONObject addThumbImageDimensionFromURL(JSONObject jobj, String url) {
		// re-calculate dimensions on the fly
		int _width = 0;
		int _height = 0;
		try {
			URL u = new URL(url);
			URLConnection conn = u.openConnection();
			InputStream stream = conn.getInputStream();
			ImageInfo info = new ImageInfo();
			info.setInput(stream);
			if (info.check() == false) {
				System.out.println("Failed to check ImageInfo ");
			} else if (info.getMimeType() == null) {
				System.out.println("Invalid MIME type");
			} else {
				_width = info.getWidth();
				_height = info.getHeight();
				System.out.println("Successfully detected image dimension for " 
				      + url + " to be " + _width +"x" + _height);
			}
		}
		catch (Exception e) {
			System.out.println("Failed to inspect image dimensions from thumbImage in " + jobj.toString(4));
			e.printStackTrace();
		}
		jobj.put("thumbWidth", _width);
		jobj.put("thumbHeight", _height);
		
		return jobj;
    }
    
    private static JSONObject copyImageDimensions(BasicDBObject dobj, JSONObject jobj) {
    	jobj.put("thumbImage", dobj.getString("thumbImage"));
    	if (dobj.containsField("thumbWidth") && dobj.containsField("thumbHeight")) {
			// already has thumb image in database
			// done with thumb images
    		jobj.put("thumbWidth", dobj.getInt("thumbWidth"));
    		jobj.put("thumbHeight", dobj.getInt("thumbHeight"));
		} else {
			// re-calculate dimensions on the fly
			jobj = addThumbImageDimensionFromURL(jobj, jobj.getString("thumbImage"));
		}
    	return jobj;
    }
}
