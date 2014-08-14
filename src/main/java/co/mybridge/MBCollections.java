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
    			ja = DBUtils.retrieveObjects("mb_collection", this, "_id", nextPath);
    		} else {
    			// check query parameters
    			String person_id = req.getParameter("person_id");
    			if (person_id != null && person_id.length() > 5) {
    				String srchFields[] = new String[2];
    				srchFields[0] = "person_id";
    				srchFields[1] = person_id;
    				ja = DBUtils.retrieveObjects("mb_collection", this, srchFields);
    			} else {
    				ja = DBUtils.retrieveObjects("mb_collection", this, "no");
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
			retJ.put("person_id", bobj.getString("person_id"));
			retJ.put("collection_title", bobj.getString("collection_title"));
		    List<String> contL = (List<String>)bobj.get("contents"); 
		    if (contL != null && contL.size() > 0) {
		    	for (String c: contL) {
		    		retJ.append("contents", c);
		    	}
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
			retB.put("person_id", jobj.getString("person_id"));
			retB.put("collection_title", jobj.getString("collection_title"));
			JSONArray cont = jobj.getJSONArray("contents");
    		ArrayList<String> contL = new ArrayList<String>();
    		for (int j = 0; j< cont.length(); j++) {
    			// verify string
    			String x = cont.getString(j);
    			contL.add(x);
    		}
    		retB.append("contents", contL);
			return retB;
		} catch(Exception x) {
			System.out.println("Failed to convert JSON to BasicDBObject: " + x.getMessage());
			System.out.println(jobj.toString(4));
			x.printStackTrace();
		}
		return null;
	}

}
