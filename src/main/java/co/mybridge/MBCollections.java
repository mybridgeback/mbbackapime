package co.mybridge;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.DB;

@WebServlet(
        name = "MBCollections",
        urlPatterns = {"/api/collections"}
    )
public class MBCollections extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 721661826713990843L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        out.write("hello B".getBytes());
        out.flush();
        out.close();
        
        DB db = DBUtils.getMongoDB();
    }

}
