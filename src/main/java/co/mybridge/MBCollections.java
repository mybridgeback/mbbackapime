package co.mybridge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;

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
        JSONArray ja = null;
        try {  
        	// check specific collection id
    		String nextPath = req.getPathInfo();
    		if (nextPath != null && nextPath.length() > 5) {
    			// there is a collection id in the path, use it
    			if (nextPath.startsWith("/")) {
    				nextPath = nextPath.substring(1);
    			}
    			if (nextPath.indexOf('/') > 0) {
    				nextPath = nextPath.substring(0, nextPath.indexOf('/'));
    			}
    			System.out.println("Loading collection with _id=" + nextPath);
    			ja = DBUtils.retrieveObjects(req, "mb_collection", this, "_id", nextPath);
    		} else {
    			// check query parameters
    			String person_id = req.getParameter("personId");
    			if (person_id != null && person_id.length() > 5) {
    				String srchFields[] = new String[2];
    				srchFields[0] = "personId";
    				srchFields[1] = person_id;
    				ja = DBUtils.retrieveObjects(req, "mb_collection", this, srchFields);
    			} else {
    				ja = DBUtils.retrieveObjects(req, "mb_collection", this, "no");
    			}
    		}
        	String outStr = ja.toString(4);
        	
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
	 *   person_id: ObjectID,
	 *   collection_title:  String,
	 *   contents: [
	 *       content_id, ...
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
		    BasicDBList dblist = (BasicDBList)bobj.get("knowledge"); 
		    if (dblist != null && dblist.size() > 0) {
		    	JSONArray ja = new JSONArray();
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
		    	retJ.put("knowledge", ja);
		    }
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
				retB.put("_id", jobj.getString("_id"));
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

}
