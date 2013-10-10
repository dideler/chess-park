package chess;

import java.io.*;

import com.sun.net.httpserver.*;

/**
 * 
 * @author Graham
 *
 * Handles an incoming HTTP connection
 */
public class RedirectHandler implements HttpHandler {

    final private String url;
    public RedirectHandler(String url) {
        this.url = url;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        Server.redirect(t, url);
        return;
    }
}
