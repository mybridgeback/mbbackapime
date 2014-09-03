package co.mybridge;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        	JSONObject requestJSON = determineRequestDispatcher(req);
        	if (requestJSON == null) {
        		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to process request");
        	}
            if (requestJSON.getString("type").equals("people")) {
            	if (requestJSON.has("personOBJ")) {
            		// already has personID and retrieved from determineRequestDispatcher
            		// return this person info
            		outStr = requestJSON.getJSONObject("personOBJ").toString(4);
            	} else {
            		// no personID, check query parameters and run query
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
    	    					srchFields[s] = "profession";
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
            } else if (requestJSON.getString("type").equals("collections")) {
            	if (requestJSON.has("collectionOBJ")) {
            		outStr = requestJSON.getJSONObject("collectionOBJ").toString(4);
            	} else {
            		// retrieve all collections for this person
            		MBCollections mbColl = new MBCollections();
            		String person_id = requestJSON.getJSONObject("personOBJ").getString("_id");
        			String srchFields[] = new String[2];
        			srchFields[0] = "personId";
        			srchFields[1] = person_id;
        			ja = DBUtils.retrieveObjects(req, "mb_collection", mbColl, srchFields);
        			outStr = ja.toString(4);
            	}
            } else if (requestJSON.getString("type").equals("knowledge")) {
            	String nextPath = "";
            	if (requestJSON.has("nextPath")) {
            		nextPath = requestJSON.getString("nextPath");
            	}
            	MBKnowledge mbKnowl = new MBKnowledge();
            	if (requestJSON.has("collectionOBJ")) {
            		JSONObject collObj = requestJSON.getJSONObject("collectionOBJ");
            		JSONArray knowledgeArray = collObj.getJSONArray("knowledge");
            		int totalFields = knowledgeArray.length();
            		String valFields[] = new String[totalFields];
    				for (int j = 0; j< knowledgeArray.length(); j++) {
    					valFields[j] = knowledgeArray.getJSONObject(j).getString("knowledgeId");
    				}
    				Map<String, String[]> srchMap = new HashMap<String, String[]>();
    				srchMap.put("_id", valFields);
    				outStr = mbKnowl.getKnowledge(req, nextPath, srchMap);
            	} else {
            		outStr = mbKnowl.getKnowledge(req, nextPath, null);
            	}
            }
        	if (outStr == null) {
        		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to get content by ID");
        	}
        	resp.setContentType("application/json");
        	resp.setContentLength(outStr.length());
        	out.write(outStr.getBytes());
        }
        catch(Exception x) {       	
        	System.out.println("Exception: " + x.getMessage());
        	x.printStackTrace();
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to retrieve data");
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
        	JSONObject requestOBJ = determineRequestDispatcher(req);
        	System.out.println("requestOBJ " + requestOBJ.toString(4));
        	if ("people".equals(requestOBJ.getString("type"))) {
        		String pid = null;
        		if (requestOBJ.has("personOBJ")) {
        			pid = requestOBJ.getJSONObject("personOBJ").getString("_id");
        		}
		        String newId = updatePerson(req, pid);
		        if (newId.length() > 4) {
		        	// retrieve this newly added person
		        	JSONArray ja = DBUtils.retrieveObjects(req, "mb_person", this, "_id", newId);
		        	JSONObject oneObj = ja.getJSONObject(0);
		        	String outStr = oneObj.toString(4);     	
		        	resp.setContentLength(outStr.length());
		        	out.write(outStr.getBytes());
		        	out.flush();
		        } 
        	} else if ("collections".equals(requestOBJ.getString("type"))) {
        		String personId = requestOBJ.getJSONObject("personOBJ").getString("_id");
        		//
        		// create a new collection for this person based on request parameters
        		MBCollections mbColl = new MBCollections();
        		String collId = mbColl.updateCollection(req, personId, null);
        		System.out.println("Successfully inserted a collection, with assigned ID=" + collId);
        		JSONArray ja = DBUtils.retrieveObjects(req, "mb_collection", mbColl, "_id", collId);
	        	JSONObject oneObj = ja.getJSONObject(0);
	        	String outStr = oneObj.toString(4);     	
	        	resp.setContentLength(outStr.length());
	        	out.write(outStr.getBytes());
	        	out.flush();
        	} else if ("knowledge".equals(requestOBJ.getString("type"))) {
        		// later
        		String personId = requestOBJ.getJSONObject("personOBJ").getString("_id");
        		String collectionId = requestOBJ.getJSONObject("collectionOBJ").getString("_id");
        		MBKnowledge knowl = new MBKnowledge();
        		String knowId = knowl.updateKnowledge(req, collectionId, null);
        		JSONArray ja = DBUtils.retrieveObjects(req, "mb_knowledge", knowl, "_id", knowId);
	        	JSONObject oneObj = ja.getJSONObject(0);
	        	String outStr = oneObj.toString(4);     	
	        	resp.setContentLength(outStr.length());
	        	out.write(outStr.getBytes());
	        	out.flush();
        	}
        }
        catch (Exception x) {
        	x.printStackTrace();
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
    		if (p.containsField("education")) {
    			retObj.put("education", p.getString("education"));
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
    			ObjectId oid = new ObjectId(p.getString("_id"));
    			retObj.put("_id", oid);
    			System.out.println("set OID=" + oid.toString());
    		}
    		String fname = p.getString("fullName");
    		retObj.put("fullName", fname);
    		
    		if (p.has("position")) {
    			retObj.put("position", p.getString("position"));
    		}
    		if (p.has("company")) {
    			retObj.put("company", p.getString("company"));
    		}
    		if (p.has("education")) {
    			retObj.put("education", p.getString("education"));
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
	
	/**
	 * this method updates or add a person record, depending on whether there is presonId
	 * @param req
	 * @param personId
	 * @return   a personId
	 * @throws Exception
	 */
    private String updatePerson(HttpServletRequest req, String personId) throws Exception {
    	try {
	        String email = req.getParameter("email");
	        String password = req.getParameter("password");
	        String industry[] = req.getParameterValues("industry");
	        String profession[] = req.getParameterValues("profession");
	        String fullname = req.getParameter("fullName");
	        JSONObject jobj = new JSONObject(); 
	        if (personId != null && personId.length() > 4) {
	        	jobj.put("_id", personId);
	        }
	        jobj.put("fullName", fullname);
	        if (email != null && password != null) {
	        	// encrypt password here
	        	String passwordHash = PasswordTool.generatePasswordHash(password);
	        	jobj.put("email", email).put("password", passwordHash);
	        }
	        for (int x=0; x<industry.length; x++) {
	            jobj.append("industries", industry[x]);
	        }
	        for (int y=0; y<profession.length; y++) {
	            jobj.append("professions", profession[y]);
	        }
	        String thumbImage = req.getParameter("thumbImage");
	        if (thumbImage != null && thumbImage.length() > 4) {
	        	jobj.put("thumbImage", thumbImage);
	        	jobj = DBUtils.addThumbImageDimensionFromURL(jobj, thumbImage);
	        }
	        
	        String position = req.getParameter("position");
	        String company = req.getParameter("company");
	        String education = req.getParameter("education");
	        if (position != null && position.length() > 1) {
	        	jobj.put("position", position);
	        }
	        if (company != null && company.length() > 1) {
	        	jobj.put("company", company);
	        }
	        if (education != null && education.length() > 1) {
	        	jobj.put("education", education);
	        }

        	return DBUtils.updateObject("mb_person", this, jobj);
        }
        catch(Exception x) {
        	x.printStackTrace();
        	throw x;
        }
    }
    
    /**
     * This will get request of /api/people/....
     * and figure out what further request this is, the possible requests are:
     *  /api/people                          -- query people
     *  /api/people/<personId> 
     *  /api/people/<personId>/collections   -- to query or (with POST parameters) update a person's collection
     *  /api/people/<personId>/collections/<collectionId>
     *  /api/people/<personId>/collections/<collectionId>/knowledge
     * 
     * @param req
     * @return    a JSONObject in the form of 
     *       <code>{  "type": "people"|"collections"|"knowledge",
     *                "personOBJ" :      -- a JSONObject for this person
     *                "collectionOBJ":   -- a JSONObject for a collection
     *                "knwoledgeOBJ":    -- a JSONObject for knowledge
     *                "nextPath":  the remaining path after /knowledge/
     *             }</code>
     * @throws UnknownHostException 
     *             
     */
    private JSONObject determineRequestDispatcher(HttpServletRequest req) throws UnknownHostException {
		JSONObject retO = new JSONObject();
		
    	// check specific person id
    	// we are already in "/api/people/"
		String nextPath = req.getPathInfo();
		if (nextPath != null && nextPath.length() > 5) {
			// there is a person id in the path, use it
			if (nextPath.startsWith("/")) {
				nextPath = nextPath.substring(1);
			}
			
			String _pid = nextPath;
			if (nextPath.indexOf('/') > 0) {
				// there is personId and then something else
				// get personId upto /
				_pid = nextPath.substring(0, nextPath.indexOf('/'));
				// nextPath is after personId, including /, would be / only
				nextPath = nextPath.substring(nextPath.indexOf('/'));
				if (nextPath.equals("/") || nextPath.length() < 3) {
					// no meaningful path
					nextPath = "";
				}  else if (nextPath.startsWith("/")) {
					nextPath = nextPath.substring(1);
				}
			} else {
				nextPath = "";
			}
			try {
				JSONArray ja = DBUtils.retrieveObjects(req, "mb_person", this, "_id", _pid);
				if (ja.length() != 1) {
					System.out.println("Invalid personId.");
					return null;
				}
				retO.put("personOBJ", ja.getJSONObject(0));
			} catch(Exception x) {
				System.out.println("Failed to access MongoDB with personId" + x.getMessage());
				return null;
			}

			// now check for /collections/
			if (nextPath.length() > 2 && nextPath.startsWith("collections")) {
				if (nextPath.matches("collections/[a-zA-Z_0-9]+.*")) {
					// get collection object
					String collId = nextPath.substring(12);
					int nextSlash = collId.indexOf("/");
					if (nextSlash > 1) {
						// set nextPath to be a sub string after collectionId, including slash
						nextPath = collId.substring(nextSlash);
						collId = collId.substring(0, nextSlash);

						if (nextPath.length() > 5) {
							if (nextPath.startsWith("/")) {
								nextPath = nextPath.substring(1);
							}
						} else {
							nextPath = "";
						}
					} else {
						nextPath = "";
					}

					MBCollections mbcoll = new MBCollections();
					String collStr = mbcoll.getCollections(req, collId);
					JSONObject collOBJ = new JSONObject(collStr);
					retO.put("collectionOBJ", collOBJ);
					
					if (nextPath.startsWith("knowledge")) {
						retO.put("type", "knowledge");
						if (nextPath.matches("knowledge/[a-zA-Z_0-9]+.*")) {
							retO.put("nextPath", nextPath.substring(10));
						} else {
							retO.put("nextPath", "");
						}
					} else {
						retO.put("type", "collections");
						retO.put("nextPath", nextPath);
					}
				} else if (nextPath.startsWith("collections")){
					// collections, but no collectionId
					retO.put("type", "collections");
					retO.put("nextPath", "");
				}
				
			} else {
				retO.put("type", "people");
			}
		} else {
			// /api/people/ request, without personId
			retO.put("type", "people");
		}
		return retO;
    }
}
