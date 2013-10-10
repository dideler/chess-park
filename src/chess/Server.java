package chess;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import chess.game.*;
import chess.lobby.*;

import com.sun.net.httpserver.*;

import eventserver.EventServer;

public class Server {	

	private static Server instance = null;

	public static Server getInstance() {
		if (instance == null)
			instance = new Server();
		return instance;
	}

	private Hashtable<String, ClientInfo> handleTable = new Hashtable<String, ClientInfo>();
	private Hashtable<Integer, ClientInfo> playerTable = new Hashtable<Integer, ClientInfo>();
	protected Hashtable<Integer, ChessGame> tables = new Hashtable<Integer, ChessGame>();
	
	public Collection<ChessGame> getTables() {
		return tables.values();
	}

	//public EventServer<LobbyEvent> lobbyServer = new EventServer<LobbyEvent>(100);
	public EventServer<LobbyEvent> lobbyServer = new EventServer<LobbyEvent>(100);

	
	public synchronized ClientInfo lookupClient(String handle) {
		ClientInfo info = handleTable.get(handle);
		if (info == null) {
			info = new ClientInfo(handle);
			handleTable.put(info.handle, info);
			playerTable.put(info.pid, info);
		}
		return info;
	}

	public synchronized ClientInfo lookupClient(int pid) {
		return playerTable.get(pid);
	}

	public synchronized ChessGame findTable(int tid) {
		return tables.get(tid);
	}

	public synchronized ChessGame findOrMakeTable(ClientInfo client, int tid) {
		ChessGame game = tables.get(tid);
		if (game == null) {
			game = new ChessGame(client,null,tid);
			tables.put(game.tid, game);
			lobbyServer.addEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.NEW));
			//AddLobbyEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.NEW));
		}
		return game;
	}

	static private int lasttid = 0;

	public synchronized ChessGame createTable(ClientInfo client) {
		while (tables.get(++lasttid) != null)
			;
		ChessGame game = new ChessGame(client,null,lasttid);
		tables.put(game.tid, game);
		lobbyServer.addEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.NEW));
		//AddLobbyEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.NEW));
		return game;
	}
	
	public synchronized boolean joinTable(ChessGame game, ClientInfo client) {
		if (game.getWhitePlayer() == null) {
			game.setWhitePlayer(client);
			if (game.getBlackPlayer() != null){
				lobbyServer.addEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.FILL));
				//AddLobbyEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.FILL));
			}
			return true;
		}
		if (game.getBlackPlayer() == null) {
			game.setBlackPlayer(client);
			if (game.getWhitePlayer() != null){
				lobbyServer.addEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.FILL));
				//AddLobbyEvent(new LobbyEvent(game.tid, LobbyEvent.EventType.FILL));
			}
			return true;
		}
		return false;
	}

	/**
	 * Redirects the client to another page with HTTP 303 Don't forget to return
	 * from your Handler after calling this. Calling any code following the call
	 * to redirect will still be executed.
	 * 
	 * @param t
	 * @param location
	 */
	public static void redirect(HttpExchange t, String location) {
		try {
			t.getResponseHeaders().set("Location", location);
			t.sendResponseHeaders(303, 0);
			t.getResponseBody().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendResponse(HttpExchange t, int code, String response) {
		try {
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes(), 0, response.length());
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a file into a byte array, returning null on fail
	 * 
	 * @param file
	 * @return
	 */

	public static byte[] readFile(File file) {
		try {
			byte[] bytes = new byte[(int) file.length()];
			BufferedInputStream bis;
			bis = new BufferedInputStream(new FileInputStream(file));
			bis.read(bytes, 0, bytes.length);
			bis.close();
			return bytes;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return null;
	}

	public static byte[] readFile(String filename) {
		return readFile(new File(filename));
	}

	/**
	 * Gets a cookie value Assumes cookies are listed as Cookie: key1=value1;
	 * key2=value2
	 * 
	 * @param t
	 * @param key
	 * @return
	 */
	public static String getCookie(HttpExchange t, String key) {
		String regex = " *" + key + "=.*";
		Set<Entry<String, List<String>>> es = t.getRequestHeaders().entrySet();
		for (Entry<String, List<String>> e : es) {
			if (e.getKey().equals("Cookie")) {
				List<String> cookies = e.getValue();
				for (String cookie : cookies) {
					String[] entries = cookie.split(";");
					for (String entry : entries) {
						if (entry.matches(regex)) {
							return entry.split("=", 2)[1];
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Recursively crawl directories, create a FileHandler for each file, and
	 * create a context for the server for each file
	 * 
	 * @param server
	 *            What serves the files
	 * @param files
	 *            An array of files and/or directories to serve
	 * @param path
	 *            Where files should be served from
	 */
	private static void createFileHandlers(HttpServer server, File[] files,
			String mountDir) {
		for (File file : files) {
			String mountPath = mountDir + file.getName();
			if (file.isDirectory()) {
				String newpath = mountPath + "/";
				createFileHandlers(server, file.listFiles(), newpath);
			} else {
				// System.out.println("Mounting " + mountPath);
				server.createContext(mountPath, new FileHandler(file));
			}
		}
	}

	public static void main(String[] args) {

		try {
			// Start the server
			HttpServer server;
			server = HttpServer.create(new InetSocketAddress(22222), 0);

			// Attach Handlers
			server.createContext("/", new RedirectHandler("/chess/lobby"));
			server.createContext("/chess/login", new LoginHandler());

			server.createContext("/chess/lobby", new LobbyHandler());
			server.createContext("/chess/lobbylistener",
					new LobbyListenerHandler());
			server.createContext("/chess/lobbylisten", new LobbyListenHandler());
			
			server.createContext("/chess/newtable", new NewTableHandler());			
			server.createContext("/chess/game", new GameHandler());
			server.createContext("/chess/listener", new ListenerHandler());
			server.createContext("/chess/listen", new ListenHandler());
			server.createContext("/chess/move", new MoveHandler());

			// this shuts down the server
			server.createContext("/chess/stop", new ExitHandler());

			// Attach text file handlers
			createFileHandlers(server, new File("public").listFiles(),
					"/chess/");

			server.setExecutor(null); // creates a default executor
			server.start();

			JOptionPane.showMessageDialog(null, "Press OK to close server");
			System.exit(0);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
