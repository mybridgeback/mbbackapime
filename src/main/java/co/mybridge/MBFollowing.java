package co.mybridge;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

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
        name = "MBFollowings",
        urlPatterns = {"/api/following/*"}
    )
public class MBFollowing extends HttpServlet implements MBConverter {

    /**
	 * 
	 */
	private static final long serialVersionUID = 721661826713990857L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        try {  
        	// check specific collection id
    		String nextPath = req.getPathInfo();
    		String outStr = getFollowings(req, nextPath, req.getParameterMap());
        	
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
	 * The JSONObject for Followings are like:
	 * <pre>
	 * { _id: ObjectID,
	 *   followerId: 
	 *   contentType:  String,
	 *   followedId:    
	 * }</pre>
	 */
	@Override
	public JSONObject convertBasicDBToJSON(BasicDBObject bobj) {
		JSONObject retJ = new JSONObject();
		try {
			if (bobj.containsField("_id")) {
				retJ.put("_id", bobj.getString("_id"));
			}
			retJ.put("followerId", bobj.getString("followerId"));
			retJ.put("contentType", bobj.getString("contentType"));
			retJ.put("followed", bobj.getString("followedId"));
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
			retB.put("followerId", jobj.getString("followerId"));
			retB.put("contentType", jobj.getString("contentType"));
			retB.put("followedId", jobj.getString("followedId"));
			
			return retB;
		} catch(Exception x) {
			System.out.println("Failed to convert JSON to BasicDBObject: " + x.getMessage());
			System.out.println(jobj.toString(4));
			x.printStackTrace();
		}
		return null;
	}
	
	public String getFollowings(HttpServletRequest req, String nextPath, Map<String, String[]> paramMap) throws UnknownHostException {
		JSONArray ja = null;
		String outStr = null;
		if (nextPath != null && nextPath.length() > 5) {
			// there is a follow id in the path, use it
			if (nextPath.startsWith("/")) {
				nextPath = nextPath.substring(1);
			}
			if (nextPath.indexOf('/') > 0) {
				nextPath = nextPath.substring(0, nextPath.indexOf('/'));
			}

			ja = DBUtils.retrieveObjects(req, "mb_follow", this, "_id", nextPath);
			if (ja.length() != 1) {
				return null;
			}
			JSONObject oneObj = ja.getJSONObject(0);
			outStr = oneObj.toString(4);
		} else {
			// check query parameters
			int totalFields = 0;
			for (String[] val : paramMap.values() ) {
				totalFields += val.length * 2;
			}
			String srchFields[] = new String[totalFields];
			String person_ids[] = req.getParameterValues("followerId");
			if (person_ids != null && person_ids.length > 0) {
				for (int i = 0; i < person_ids.length; i++) {
					srchFields[i * 2] = "followerId";
					srchFields[i*2 + 1] = person_ids[i];
				}
			}
			
			String content_types[] = req.getParameterValues("contentType");
			if (content_types != null && content_types.length > 0) {
				for (int i = 0; i < content_types.length; i++) {
					srchFields[i * 2] = "contentType";
					srchFields[i*2 + 1] = content_types[i];
				}
			}
			if (totalFields > 1)
				ja = DBUtils.retrieveObjects(req, "mb_follow", this, srchFields);
		    else 
		    	ja = DBUtils.retrieveObjects(req, "mb_cfollow", this, "no");
			
			outStr = ja.toString(4);
		}
    	return outStr;
	}
	
	/**
	 * this method updates or add a mb_follow record, depending on whether there is presonId, collectionId
	 * @param req
	 * @param followId   null if creating an new mb_follow, or update existing mb_follow record
	 * @param followerId 
	 * @param contentType 
	 * @param followedId 
	 * @return   modified followId
	 * @throws Exception
	 */
    public String updateFollowing(HttpServletRequest req, String followId, String followerId, String contentType, String followedId) throws Exception {
    	try {	        
	        JSONObject jobj = new JSONObject(); 
	        if (followId != null && followId.length() > 4) {
	        	jobj.put("_id", followId);
	        }
	        jobj.put("followerId", followerId);
	        jobj.put("contentType", contentType);
	        jobj.put("followedId", followedId);

        	return DBUtils.updateObject("mb_follow", this, jobj);
        }
        catch(Exception x) {
        	x.printStackTrace();
        	throw x;
        }
    }
}
