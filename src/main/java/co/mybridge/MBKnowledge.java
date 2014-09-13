package co.mybridge;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

@WebServlet(
        name = "MBKnoweledge",
        urlPatterns = {"/api/knowledge/*"}
    )
public class MBKnowledge extends HttpServlet implements MBConverter {

    /**
	 * 
	 */
	private static final long serialVersionUID = 721661826713990846L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        String outStr = null;

		String nextPath = req.getPathInfo();
		Map<String, String[]> paramMap = req.getParameterMap();
		outStr = getKnowledge(req, nextPath, paramMap);

		if (outStr == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to get knowledge");
		}
        resp.setContentType("application/json");
        resp.setContentLength(outStr.length());
        out.write(outStr.getBytes());
        out.flush();
        out.close();
    }

	/**
	 * The JSONObject for content is:
	 *    { _id: ObjectID,
     *       title: String,
     *       externalURL: String,
     *       htmlBody: String,
     *       width: number,
     *       height: number
     *    }
	 */
	@Override
	public JSONObject convertBasicDBToJSON(BasicDBObject bobj) {
		JSONObject retJ = new JSONObject();
		try {
			if (bobj.containsField("_id")) {
				ObjectId myId = bobj.getObjectId("_id");
    			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
    			df.setTimeZone(TimeZone.getTimeZone("GMT"));
    			java.util.Date cd = myId.getDate();
    			retJ.put("_id", myId.toString());
    			retJ.put("createTimeMillis", cd.getTime());
    			retJ.put("createTimeGMT", df.format(cd));
			}
			retJ.put("title", bobj.getString("title"));
			retJ.put("width", bobj.getInt("width"));
			retJ.put("height", bobj.getInt("height"));
			if (bobj.containsField("htmlBody")) {
				retJ.put("htmlBody", bobj.getString("htmlBody"));
			} 
			if (bobj.containsField("externalURL")){
				retJ.put("externalURL", bobj.getString("externalURL"));
			}
			if (bobj.containsField("contentSource")) {
				retJ.put("contentSource", bobj.getString("contentSource"));
			}
			if (bobj.containsField("contentType")) {
				retJ.put("contentType", bobj.getString("contentType"));
			}
			if (bobj.containsField("publisher")) {
				retJ.put("publisher", bobj.getString("publisher"));
			}
			return retJ;
		}
		catch(Exception e) {
			System.out.println("Failed to convert BasicDBObject to JSON: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BasicDBObject convertJSONToBasicDB(JSONObject jobj) {
		BasicDBObject  retB = new BasicDBObject();
		if (jobj == null)   return null;
		try {
			if (jobj.has("_id")) {
				ObjectId oid = new ObjectId(jobj.getString("_id"));
				retB.put("_id", oid);
			}
			retB.put("title", jobj.getString("title"));
			retB.put("width", jobj.getInt("width"));
			retB.put("height", jobj.getInt("height"));
			if (jobj.has("htmlBody")) {
				retB.put("htmlBody", jobj.getString("htmlBody"));
			} 
			if (jobj.has("externalURL")) {
				retB.put("externalURL", jobj.getString("externalURL"));
			}
			if (jobj.has("contentSource")) {
				retB.put("contentSource", jobj.getString("contentSource"));
			}
			if (jobj.has("contentType")) {
				retB.put("contentType", jobj.getString("contentType"));
			}
			if (jobj.has("publisher")) {
				retB.put("publisher", jobj.getString("publisher"));
			}
			return retB;
		} catch(Exception e) {
			System.out.println("Failed to convert JSON to BasicDB: " + e.getMessage());
			System.out.println(jobj.toString(4));
			e.printStackTrace();
		}
		return null;
	}

	public String getKnowledge(HttpServletRequest req, String nextPath, Map<String, String[]> paramMap) {
		JSONArray ja = null;
        try {
	        // check specific content id
			if (nextPath != null && nextPath.length() > 5) {
				// there is a content id in the path, use it
				if (nextPath.startsWith("/")) {
					nextPath = nextPath.substring(1);
				}
				if (nextPath.indexOf('/') > 0) {
					nextPath = nextPath.substring(0, nextPath.indexOf('/'));
				}

				ja = DBUtils.retrieveObjects(req, "mb_knowledge", this, "_id", nextPath);
				if (ja.length() == 1) {
					JSONObject oneObj = ja.getJSONObject(0);
					return oneObj.toString(4);
				} else {
					return null;
				}
			} else if (paramMap != null && paramMap.size() > 0) {
				int totalFields = 0;
				for (String[] val : paramMap.values() ) {
					totalFields += val.length * 2;
				}
				String srchFields[] = new String[totalFields];
				int i = 0;
				for (String key: paramMap.keySet()) {
					String[] vals = paramMap.get(key);
					for (int j = 0; j< vals.length; j++) {
						srchFields[i] = key;
						srchFields[i+1] = vals[j];
						i = i + 2;
					}
				}
				ja = DBUtils.retrieveObjects(req, "mb_knowledge", this, srchFields);
			} else {
				ja = DBUtils.retrieveObjects(req, "mb_knowledge", this, "no");
			}
			return ja.toString(4);
        } catch(Exception e) {
        	System.out.println("Failed to load contents: " + e.getMessage());
        	e.printStackTrace();
        	return null;
        }
	}
	/**
	 * this method updates or add a knowledge record, depending on whether there is presonId, collectionId
	 * @param req
	 * @param personId
	 * @Param collectionId   null if creating an new collection, or update existing collectionTitle
	 * @return   knowledgeId
	 * @throws Exception
	 */
    public String updateKnowledge(HttpServletRequest req, String collectionId, String knowledgeId) 
    		throws Exception {
    	try {
    		MBCollections mbcoll = new MBCollections();
    		
	        String title = req.getParameter("title");
	        String contentSource = req.getParameter("contentSource");
	        String publisher = req.getParameter("publisher");
	        String contentType = req.getParameter("contentType");
	        String externalURL = req.getParameter("externalURL");
	        String htmlBody = req.getParameter("htmlBody");
	        String thumbImage = req.getParameter("thumbImage");
	        String width = req.getParameter("width");
	        String height = req.getParameter("height");
	        
	        
	        JSONObject jobj = new JSONObject(); 
	        jobj.put("title", title);
	        if (contentSource != null && contentSource.length() > 1) {
	        	jobj.put("contentSource", contentSource);
	        }
	        if (publisher != null && publisher.length() > 1){
	        	jobj.put("publisher", publisher);
	        }
	        if (contentType != null && contentType.length() > 1) {
	        	jobj.put("contentType", contentType);
	        }
	        jobj.put("externalURL", externalURL);
	        jobj.put("htmlBody", htmlBody);
	        jobj.put("thumbImage", thumbImage);
	        jobj.put("width", Integer.parseInt(width));
	        jobj.put("height", Integer.parseInt(height));

        	String knowlId = DBUtils.updateObject("mb_knowledge", this, jobj);
        	
        	// load collection and add this in
        	String srchFields[] = new String[2];
        	srchFields[0] = "_id";
        	srchFields[1] = collectionId;
        	JSONArray ja = DBUtils.retrieveObjects(null, "mb_collection", mbcoll, srchFields);
        	JSONObject collObj = ja.getJSONObject(0);

        	String customDescription = req.getParameter("customDescription");
        	JSONArray knowArray = collObj.getJSONArray("knowledge");
        	knowArray.put(new JSONObject().put("knowledgeId", knowlId).put("customDescription", customDescription));
        	collObj.put("knowledge", knowArray);
        	
        	DBUtils.updateObject("mb_collection", mbcoll, collObj);
        	return knowlId;
        }
        catch(Exception x) {
        	x.printStackTrace();
        	throw x;
        }
    }
}
