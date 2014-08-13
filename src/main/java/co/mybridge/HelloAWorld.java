package co.mybridge;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(
        name = "HelloAWorld",
        urlPatterns = {"/user/new"}
    )
public class HelloAWorld extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1850007914403256775L;
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        out.write("hello A".getBytes());
        out.flush();
        out.close();
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String industry = req.getParameter("industry");
        String profession = req.getParameter("profession");
        String fullname = req.getParameter("fullname");
        JSONObject pobj = new JSONObject();
        Random r = new Random();
        int ri = Math.abs(r.nextInt() % 10000);
        pobj.put("person_id", email + "__" + ri).put("email", email).put("password", password)
             .append("industries", industry).append("professions", profession).put("fullname", fullname);
        JSONArray ja = null;
        try {
        	DBUtils.addPerson(pobj);
        	ja = DBUtils.retrieveObjects("mb_user", "no");
        	out.write("All Registered Users: ".getBytes());
        	out.write(ja.toString(4).getBytes());
        }
        catch(Exception x) {
        	out.write(("Exception: " + x.getMessage()).getBytes());
        }
        out.flush();
        out.close();
    }
}
