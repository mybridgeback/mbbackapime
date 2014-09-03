package co.mybridge;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@WebServlet(
        name = "MBCollections",
        urlPatterns = {"/api/collections/*"}
    )
public class MBCollections extends HttpServlet implements MBConverter {

    /**
	 * 
	 */
	private static final long serialVersionUID = 721661826713990843L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        try {  
        	// check specific collection id
    		String nextPath = req.getPathInfo();
    		String outStr = getCollections(req, nextPath);
        	
        	resp.setContentType("application/json");
        	resp.setContentLength(outStr.length());
        	out.write(outStr.getBytes());
        	out.flush();
        } catch(Exception e) {
        	System.out.println("Failed to get collections: " + e.getMessage());
        	e.printStackTrace();
        }
        out.close();
    }

	/**
	 * The JSONObject for Collections are like:
	 * <pre>
	 * { _id: ObjectID,
	 *   personId: ObjectID,
	 *   collectionTitle:  String,
	 *   knowledge: [
	 *       {knowledgeId, customDescription}, ...
	 *   ]
	 * }</pre>
	 */
	@Override
	public JSONObject convertBasicDBToJSON(BasicDBObject bobj) {
		JSONObject retJ = new JSONObject();
		try {
			if (bobj.containsField("_id")) {
				retJ.put("_id", bobj.getString("_id"));
			}
			retJ.put("personId", bobj.getString("personId"));
			retJ.put("collectionTitle", bobj.getString("collectionTitle"));
		    BasicDBList dblist = null;
		    if (bobj.containsField("knowledge")) {
		        dblist = (BasicDBList)bobj.get("knowledge"); 
		    }
		    JSONArray ja = new JSONArray();
		    if (dblist != null && dblist.size() > 0) {
		    	for (Object ob: dblist) {
		    		BasicDBObject bob = (BasicDBObject) ob;
		    		JSONObject job = new JSONObject();
		    		if (bob.containsField("knowledgeId") && bob.containsField("customDescription")) {
		    			job.put("knowledgeId", bob.getString("knowledgeId"));
		    			job.put("customDescription", bob.getString("customDescription"));
		    			ja.put(job);
		    		} else {
		    			System.out.println("Invalid db object for knowledge: " + bob.toString());
		    		}
		    	}
		    }
		    retJ.put("knowledge", ja);
			return retJ;
		} catch(Exception x) {
			System.out.println("Failed to convert BasicDBObject to JSON: " + x.getMessage());
			x.printStackTrace();
		}
		return null;
	}

	@Override
	public BasicDBObject convertJSONToBasicDB(JSONObject jobj) {
		BasicDBObject retB = new BasicDBObject();
		if (jobj == null)  return null;
		try {
			if (jobj.has("_id")) {
				ObjectId oid = new ObjectId(jobj.getString("_id"));
				retB.put("_id", oid);
			}
			retB.put("personId", jobj.getString("personId"));
			retB.put("collectionTitle", jobj.getString("collectionTitle"));
			if (jobj.has("knowledge")) {
				JSONArray cont = jobj.getJSONArray("knowledge");
				BasicDBList dblist = new BasicDBList();

	    		for (int j = 0; j< cont.length(); j++) {    			
	    			JSONObject ko = cont.getJSONObject(j);
	    			if (ko.has("knowledgeId") && ko.has("customDescription")) {
	    				BasicDBObject dbo = new BasicDBObject();
	    				dbo.put("knowledgeId", ko.getString("knowledgeId"));
	    				dbo.put("customDescription", ko.getString("customDescription"));
	    				dblist.add(dbo);
	    			} else {
	    				System.out.println("Invalid JSON knowledge object: " + ko.toString(4));
	    			}
	    		}
	    		retB.append("knowledge", dblist);
			}
			return retB;
		} catch(Exception x) {
			System.out.println("Failed to convert JSON to BasicDBObject: " + x.getMessage());
			System.out.println(jobj.toString(4));
			x.printStackTrace();
		}
		return null;
	}
	
	public String getCollections(HttpServletRequest req, String nextPath) throws UnknownHostException {
		JSONArray ja = null;
		String outStr = null;
		if (nextPath != null && nextPath.length() > 5) {
			// there is a collection id in the path, use it
			if (nextPath.startsWith("/")) {
				nextPath = nextPath.substring(1);
			}
			if (nextPath.indexOf('/') > 0) {
				nextPath = nextPath.substring(0, nextPath.indexOf('/'));
			}

			ja = DBUtils.retrieveObjects(req, "mb_collection", this, "_id", nextPath);
			if (ja.length() != 1) {
				return null;
			}
			JSONObject oneObj = ja.getJSONObject(0);
			outStr = oneObj.toString(4);
		} else {
			// check query parameters
			String person_ids[] = req.getParameterValues("personId");
			if (person_ids != null && person_ids.length > 0) {
				int maxfields = person_ids.length * 2;
				String srchFields[] = new String[maxfields];
				for (int i = 0; i < person_ids.length; i++) {
					srchFields[i * 2] = "personId";
					srchFields[i*2 + 1] = person_ids[i];
				}
				ja = DBUtils.retrieveObjects(req, "mb_collection", this, srchFields);
			} else {
				ja = DBUtils.retrieveObjects(req, "mb_collection", this, "no");
			}
			outStr = ja.toString(4);
		}
    	return outStr;
	}
	
	/**
	 * this method updates or add a collection record, depending on whether there is presonId, collectionId
	 * @param req
	 * @param personId
	 * @Param collectionId   null if creating an new collection, or update existing collectionTitle
	 * @return   modified collectionId
	 * @throws Exception
	 */
    public String updateCollection(HttpServletRequest req, String personId, String collectionId) throws Exception {
    	try {
	        String collectionTitle = req.getParameter("collectionTitle");
	        
	        JSONObject jobj = new JSONObject(); 
	        jobj.put("personId", personId);
	        
	        if (collectionId != null && collectionId.length() > 4) {
	        	jobj.put("_id", collectionId);
	        	//
	        	// other part of this collection
	        	//  assign other part of this collection as the update will be full replacement
	        	String srchField[] =  new String[2];
	        	srchField[0] = "_id";
	        	srchField[1] = collectionId;
	        	JSONArray ja = DBUtils.retrieveObjects(null, "mb_collection", this, srchField);
	        	JSONObject origColl = ja.getJSONObject(0);
	        	JSONArray origKnowls = origColl.getJSONArray("knowledge");
	        	jobj.put("knowledge", origKnowls);
	        }
	        
	        jobj.put("collectionTitle", collectionTitle);
        	return DBUtils.updateObject("mb_collection", this, jobj);
        }
        catch(Exception x) {
        	x.printStackTrace();
        	throw x;
        }
    }
}
