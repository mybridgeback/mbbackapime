package co.mybridge;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet(
        name = "MBLogin",
        urlPatterns = {"/api/login"}
    )
public class MBLogin extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2850007914403256775L;
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No GET request");
	}

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        if (email == null || email.length() < 2) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty email address");
        }
        try {
	        MBPeople conv = new MBPeople();
	        JSONArray pOBJs = DBUtils.retrieveObjects(req, "mb_person", conv, "email", email);
	        JSONObject pOBJ = pOBJs.getJSONObject(0);
	        if (PasswordTool.verifyPassword(password, pOBJ.getString("password"))) {
	        	resp.setContentType("application/json");
	        	String outStr = pOBJ.toString(4);     	
	        	resp.setContentLength(outStr.length());
	        	out.write(outStr.getBytes());
	        	out.flush();
	        } else {
	        	resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Incorrect email or password");
	        }
        }
        catch (Exception x) {
        	x.printStackTrace();
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to login: " + x.getMessage());
        }
    }
}
