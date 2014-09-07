package co.mybridge;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.htmlparser.Node;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet(
        name = "MBURLExtractor",
        urlPatterns = {"/api/urlextractor"}
    )
public class MBURLExtractor extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2850007914403256777L;
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
		String requrl = req.getParameter("url");
		if (requrl == null || requrl.length() < 2) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL");
			return;
		}
		String checkurl = requrl.toLowerCase();
		if (checkurl.indexOf(":") < 0 && checkurl.startsWith("http") == false && checkurl.startsWith("//") == false) {
			requrl = "http://" + requrl;
		}
		
		JSONArray possibleTitles = new JSONArray();
		JSONArray possibleImages = new JSONArray();
		
		try {
			List<String> imagesVisited = new ArrayList<String>();
			URL u = new URL(requrl);
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection uconn = (HttpURLConnection)u.openConnection();
			org.htmlparser.Parser htmlp = new org.htmlparser.Parser(uconn);
			NodeList nl = htmlp.parse(null);
			NodeList imgs = nl.extractAllNodesThatMatch (new TagNameFilter ("IMG"), true);
			NodeIterator nodeiter= imgs.elements();
			while (nodeiter.hasMoreNodes()) {
				Node n = nodeiter.nextNode();
				if (n instanceof ImageTag) {
					ImageTag in = (ImageTag)n;
					String iurl = in.getImageURL();
					if (imagesVisited.contains(iurl)) {
						continue;
					}
					imagesVisited.add(iurl);
					JSONObject x = new JSONObject();
					x.put("thumbImage", iurl);
					DBUtils.addThumbImageDimensionFromURL(x, iurl);
					if (x.getInt("thumbWidth") > 10 && x.getInt("thumbHeight") > 10) {
						possibleImages.put(x);
					} else {
						System.out.println("Ignore small image " + iurl);
					}
				}
			}
			NodeList titles = nl.extractAllNodesThatMatch (new TagNameFilter ("TITLE"), true);
			nodeiter= titles.elements();
			while (nodeiter.hasMoreNodes()) {
				Node n = nodeiter.nextNode();
				if (n instanceof TitleTag) {
					TitleTag tn = (TitleTag)n;
					possibleTitles.put(tn.getTitle());
				}
			}
			NodeList h1s = nl.extractAllNodesThatMatch (new TagNameFilter ("H1"), true);
			nodeiter= h1s.elements();
			while (nodeiter.hasMoreNodes()) {
				Node n = nodeiter.nextNode();
				if (n instanceof HeadingTag) {
					HeadingTag htg = (HeadingTag) n;
					String t = htg.getTagName();
					if (t.equals("H1")) {
						possibleTitles.put(htg.toPlainTextString());
					}
				}
			}
			
			JSONObject retOBJ = new JSONObject();
			retOBJ.put("possibleTitles", possibleTitles);
			retOBJ.put("possibleImages", possibleImages);
			String outStr = retOBJ.toString(4);     	
			resp.setContentType("application/json");
        	resp.setContentLength(outStr.length());
        	ServletOutputStream out = resp.getOutputStream();
        	out.write(outStr.getBytes());
        	out.flush();
		} catch(Exception e) {
			System.out.println("Failed in extracting url " + requrl);
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to extract URL " + requrl);
		}
	}
}
