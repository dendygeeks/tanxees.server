package bfbc.tank.core;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import bfbc.tank.core.Game.StateUpdateHandler;

@WebSocket
public class PlayerWebSocket implements StateUpdateHandler {

	// Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    private Game game = new Game(this);
    
    public PlayerWebSocket() {
		game.putFieldCellType(1, 1, CellType.CONCRETE);
		game.putFieldCellType(1, 2, CellType.CONCRETE);
		game.putFieldCellType(1, 3, CellType.CONCRETE);
		game.putFieldCellType(1, 4, CellType.CONCRETE);
		game.putFieldCellType(1, 5, CellType.CONCRETE);
		game.putFieldCellType(1, 6, CellType.CONCRETE);
		game.putFieldCellType(1, 7, CellType.CONCRETE);
		game.putFieldCellType(1, 8, CellType.CONCRETE);
		game.putFieldCellType(2, 8, CellType.CONCRETE);
		game.putFieldCellType(3, 9, CellType.CONCRETE);
		game.putFieldCellType(4, 9, CellType.CONCRETE);
		game.putFieldCellType(5, 9, CellType.CONCRETE);
		game.putFieldCellType(6, 9, CellType.CONCRETE);
		game.putFieldCellType(7, 9, CellType.CONCRETE);
		game.putFieldCellType(8, 8, CellType.CONCRETE);

    	game.putFieldCellType(4, 4, CellType.CONCRETE);
		game.putFieldCellType(4, 5, CellType.CONCRETE);
		game.putFieldCellType(5, 4, CellType.CONCRETE);
		game.putFieldCellType(5, 5, CellType.CONCRETE);
		game.putFieldCellType(6, 5, CellType.CONCRETE);
		game.putFieldCellType(5, 6, CellType.CONCRETE);
	}
    
    @Override
    public void update(Game state) {
    	broadcastString(state.toJson());
    }
    
    private void broadcastString(String string) {
    	for (Session s : sessions) {
    		try {
				s.getRemote().sendString(string);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        if (game.getState() == State.NEW) {
        	game.start();
        }
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
    	game.setPlayerCommands(PlayerCommand.fromJson(message));
    }

}
