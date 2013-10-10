package chess;

import java.io.*;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham
 *
 * Handles an incoming HTTP connection
 */
public class LoginHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {

		// get POST line
		InputStream is = (InputStream) t.getRequestBody();
		BufferedReader br = new BufferedReader(
				new InputStreamReader(is));
		
		// Tells the user error information about his handle
		String message = "Please enter a handle";

		// validate POST line
		String line = br.readLine();
		if (line != null && line.matches("^handle=.*$")) {
			// input is good, check for a valid handle
			String handle = line.split("=",2)[1];
			if (handle.matches("^[a-zA-Z0-9\\-_\\.]+$")) {
				// Valid handle, set cookie then redirect
				t.getResponseHeaders().add("Set-Cookie", "handle=" + handle);
				Server.redirect(t, "lobby");
				return;
				
			} else {
				// Invalid handle, try again
				message = "Handle can only contain letters, numbers, and the symbols \"-_.\"";
			}
		}
		
		// send response to client
		String response = "<html><head>" +
        "<title>Chess Park</title>" +
        "<link  href=\"//fonts.googleapis.com/css?family=Reenie+Beanie:regular\" rel=\"stylesheet\" type=\"text/css\">" +
        "<link type=\"text/css\" rel=\"stylesheet\" media=\"all\" href=\"css/login.css\" />" +
        "</head><body>" +
        "<div id=\"outer\">" +
        "<div id=\"header\"><h2>Chess Park</h2></div>" +
        "<div id=\"info\">" +
        "<p>" + message + "</p>" +
				"<form method=\"post\">" +
				"<input type=\"text\" name=\"handle\" />" +
				"<input type=\"submit\" class=\"loginbutton\" value=\"Enter park\" />" +
				"</form></div>" +
        "<div id=\"footer\">Copyright &copy; 2011</div>" +
        "</div></body></html>";

		t.getResponseHeaders().set("Content-type", "text/html");
		t.sendResponseHeaders(200, response.length());
		
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes(), 0, response.length());
		os.close();
	}

}
