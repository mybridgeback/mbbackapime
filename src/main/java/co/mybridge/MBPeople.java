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
        try {  
        	// check specific person id
    		String nextPath = req.getPathInfo();
    		if (nextPath != null && nextPath.length() > 3) {
    			// there is a person id in the path, use it
    			if (nextPath.startsWith("/")) {
    				nextPath = nextPath.substring(1);
    			}
    			if (nextPath.indexOf('/') > 0) {
    				nextPath = nextPath.substring(0, nextPath.indexOf('/'));
    			}
    			System.out.println("Loading person with _id=" + nextPath);
    			ja = DBUtils.retrieveObjects("mb_person", this, "_id", nextPath);
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
    				ja = DBUtils.retrieveObjects("mb_person", this, srchFields);
    			} else {
    				ja = DBUtils.retrieveObjects("mb_person", this, "no");
    			}
    		}
        	String outStr = ja.toString(4);
        	
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
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String industry[] = req.getParameterValues("industry");
        String profession[] = req.getParameterValues("profession");
        String fullname = req.getParameter("fullname");
        JSONObject pobj = new JSONObject();    
        pobj.put("fullname", fullname);
        if (email != null && password != null) {
        	pobj.put("email", email).put("password", password);
        }
        for (int x=0; x<industry.length; x++) {
            pobj.append("industries", industry[x]);
        }
        for (int y=0; y<profession.length; y++) {
            pobj.append("professions", profession[y]);
        }

        try {
        	addPerson(pobj);

        	// retrieve all people
        	JSONArray ja = DBUtils.retrieveObjects("mb_person", this, "no");
        	String outStr = ja.toString(4);     	
        	resp.setContentType("application/json");
        	resp.setContentLength(outStr.length());
        	out.write(outStr.getBytes());
        }
        catch(Exception x) {
        	x.printStackTrace();
        	out.write(("{ \"ERROR\": \"" + x.getMessage() + "\" }").getBytes());
        	out.flush();
        	out.close();
        }
    }
    
    public void addPerson(JSONObject p) throws MongoException, UnknownHostException  {
    	BasicDBObject pobj = convertJSONToBasicDB(p);
    	if (pobj == null) {
    		return;
    	}
    	try {
	    	DB  db = DBUtils.getMongoDB();
	    	DBCollection coll = db.getCollection("mb_person");
	    	coll.insert(pobj, WriteConcern.JOURNAL_SAFE);
    	}
    	catch(Exception x) {
    		x.printStackTrace(System.out);
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
    		String fname = p.getString("fullname");
    		retObj.put("fullname", fname);
    		
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
    			retObj.append("_id", p.get("_id"));
    		}
    		String fname = p.getString("fullname");
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

}
