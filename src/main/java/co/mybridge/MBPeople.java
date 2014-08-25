package co.mybridge;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

@WebServlet(
        name = "MBPeople",
        urlPatterns = {"/api/people/*"}
    )
public class MBPeople extends HttpServlet implements MBConverter {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1850007914403256775L;
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        JSONArray ja = null;
        String outStr = null;
        try {  
        	// check specific person id
    		String nextPath = req.getPathInfo();
    		if (nextPath != null && nextPath.length() > 5) {
    			// there is a person id in the path, use it
    			String _pid = nextPath;
    			if (nextPath.startsWith("/")) {
    				nextPath = nextPath.substring(1);
    				_pid = nextPath;
    			}
    			if (nextPath.indexOf('/') > 0) {
    				// there is personId and then something else
    				_pid = nextPath.substring(0, nextPath.indexOf('/'));
    				nextPath = nextPath.substring(nextPath.indexOf('/') + 1);
    			} else {
    				nextPath = "";
    			}
    			System.out.println("Loading person with _id=" + _pid);
    			ja = DBUtils.retrieveObjects(req, "mb_person", this, "_id", _pid);
    			if (nextPath.length() > 0 && ja.length() == 1) {
    				System.out.println("The person exists, proceeds with next operations: ");
    			}
    			JSONObject oneObj = ja.getJSONObject(0);
    			outStr = oneObj.toString(4);
    		} else {
    			// check query parameters
    			String industries[] = req.getParameterValues("industry");
    			String professions[] = req.getParameterValues("profession");
    			if ((industries != null && industries.length > 0) || 
    					(professions != null && professions.length > 0)) {
    				// add parameters
    				int totalFields = (((industries!=null)?industries.length:0) + ((professions!=null)?professions.length:0) ) * 2;
    				String srchFields[] = new String[totalFields];
    				int s = 0;
    				if (industries != null) {
	    				for (int x = 0; x < industries.length ; x++) {
	    					srchFields[s] = "industries";
	    					srchFields[s+1] = industries[x];
	    					s=s+2;
	    				}
    				}
    				if (professions != null) {
	    				for (int y = 0; y < professions.length ; y++) {
	    					srchFields[s] = "professions";
	    					srchFields[s+1] = professions[y];
	    					s=s+2;
	    				}
    				}
    				ja = DBUtils.retrieveObjects(req, "mb_person", this, srchFields);
    			} else {
    				ja = DBUtils.retrieveObjects(req, "mb_person", this, "no");
    			}
    			outStr = ja.toString(4);
    		}
        	
        	resp.setContentType("application/json");
        	resp.setContentLength(outStr.length());
        	out.write(outStr.getBytes());
        }
        catch(Exception x) {       	
        	System.out.println("Exception: " + x.getMessage());
        	x.printStackTrace();
        	
        	out.write(("{ \"ERROR\": \"" + x.getMessage() + "\" }").getBytes());
        }
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("application/json");
        try {
	        String newId = addPerson(req);
	        if (newId.length() > 4) {
	        	// retrieve this newly added person
	        	JSONArray ja = DBUtils.retrieveObjects(req, "mb_person", this, "_id", newId);
	        	JSONObject oneObj = ja.getJSONObject(0);
	        	String outStr = oneObj.toString(4);     	
	        	resp.setContentLength(outStr.length());
	        	out.write(outStr.getBytes());
	        	out.flush();
	        } 
        }
        catch (Exception x) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to create a user: " + x.getMessage());
        }
    }
    
    /**
     * Convert a person BasicDBObject to JSONObject. 
     * @param p
     * @return   JSONArray representing a person, or null in all other cases.
     */
	@Override
	public JSONObject convertBasicDBToJSON(BasicDBObject p) {
	   	JSONObject retObj = new JSONObject();
    	try {
    		if (p.containsField("_id")) {
    			retObj.put("_id", p.getString("_id"));
    		}
    		String fname = p.getString("fullName");
    		retObj.put("fullName", fname);
    		
    		if (p.containsField("position")) {
    			retObj.put("position", p.getString("position"));
    		}
    		if (p.containsField("company")) {
    			retObj.put("company", p.getString("company"));
    		}
    		List<String> indL = (List<String>)p.get("industries"); 		
    		for (String i : indL) {
    			retObj.append("industries", i);
    		}
    		
    		List<String> profL = (List<String>)p.get("professions");
    		for (String r: profL) {
    			retObj.append("professions", r);
    		}
    		
    		if (p.containsField("email") && p.containsField("password")) {
    			retObj.put("email", p.getString("email"));
    			retObj.put("password", p.getString("password"));
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
	
	/**
	 * this method is to take a person JSONObject, and convert it to BasicDBObject
	 * if email and password exist, then this is a registered user, otherwise, it's a leader object.
	 * @param p 
	 * @return  BasicDBObject to be saved into MongoDB, or return null if the JSONObject is not a person
	 */
	@Override
	public BasicDBObject convertJSONToBasicDB(JSONObject p) {
    	BasicDBObject retObj = new BasicDBObject();
    	try {
    		if (p.has("_id")) {
    			retObj.put("_id", p.get("_id"));
    		}
    		String fname = p.getString("fullName");
    		retObj.put("fullName", fname);
    		
    		if (p.has("position")) {
    			retObj.put("position", p.getString("position"));
    		}
    		if (p.has("company")) {
    			retObj.put("company", p.getString("company"));
    		}
    		
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
    		for (int j = 0; j< prof.length(); j++) {
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
	
	
    private String addPerson(HttpServletRequest req) throws Exception {
    	try {
	        String email = req.getParameter("email");
	        String password = req.getParameter("password");
	        String industry[] = req.getParameterValues("industry");
	        String profession[] = req.getParameterValues("profession");
	        String fullname = req.getParameter("fullName");
	        JSONObject pobj = new JSONObject();    
	        pobj.put("fullName", fullname);
	        if (email != null && password != null) {
	        	// encrypt password here
	        	String passwordHash = PasswordTool.generatePasswordHash(password);
	        	pobj.put("email", email).put("password", passwordHash);
	        }
	        for (int x=0; x<industry.length; x++) {
	            pobj.append("industries", industry[x]);
	        }
	        for (int y=0; y<profession.length; y++) {
	            pobj.append("professions", profession[y]);
	        }
	        String thumbImage = req.getParameter("thumbImage");
	        if (thumbImage != null && thumbImage.length() > 4) {
	        	pobj.put("thumbImage", thumbImage);
	        	pobj = DBUtils.addThumbImageDimensionFromURL(pobj, thumbImage);
	        }
	        
	        String position = req.getParameter("position");
	        String company = req.getParameter("company");
	        if (position != null && position.length() > 1) {
	        	pobj.put("position", position);
	        }
	        if (company != null && company.length() > 1) {
	        	pobj.put("company", company);
	        }

        	return DBUtils.updateObject("mb_person", this, pobj);
        }
        catch(Exception x) {
        	x.printStackTrace();
        	throw x;
        }
    }
}
