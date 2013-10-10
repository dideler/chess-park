package chess;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * @author Graham
 *
 * Handles an incoming HTTP connection
 */
public class ExitHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		t.sendResponseHeaders(200, 0);
		t.getResponseBody().close();
		System.exit(0);
	}

}
