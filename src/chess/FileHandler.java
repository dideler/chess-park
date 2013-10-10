package chess;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * @author Graham
 *
 * Handles an incoming HTTP connection
 */
public class FileHandler implements HttpHandler {
	final File file;

	public FileHandler(File file) {
		this.file = file;
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		// Load the file into memory
		byte[] bytes = Server.readFile(file);

		// Send response to client
		t.sendResponseHeaders(200, bytes.length);
		OutputStream os = t.getResponseBody();
		os.write(bytes, 0, bytes.length);
		os.close();
	}

}
