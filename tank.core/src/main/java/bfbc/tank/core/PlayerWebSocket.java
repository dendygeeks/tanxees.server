package bfbc.tank.core;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.Game.StateUpdateHandler;

@WebSocket
public class PlayerWebSocket implements StateUpdateHandler {

	// Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    private Map<Integer, Session> controlledPlayers = new HashMap<>();
    
    private Game game = new Game(this);
    
    
    
    public PlayerWebSocket() {
    	// Walls
    	for (int i = 0; i < game.fieldHeight - 1; i++) {
    		game.putFieldCellType(0, i, CellType.C);
    	}
    	for (int i = 1; i < game.fieldHeight; i++) {
    		game.putFieldCellType(game.fieldWidth - 1, i, CellType.C);
    	}
    	for (int i = 1; i < game.fieldWidth; i++) {
    		game.putFieldCellType(i, 0, CellType.C);
    	}
    	for (int i = 0; i < game.fieldWidth - 1; i++) {
    		game.putFieldCellType(i, game.fieldHeight - 1, CellType.C);
    	}

    	int thirdH = (game.fieldHeight + 1) / 3 / 2 * 2;
    	// Cells
		for (int i = 1; i < game.fieldWidth - 1; i++) {
			game.putFieldCellType(i, thirdH - 1, CellType.B);
			game.putFieldCellType(i, thirdH, CellType.B);
			game.putFieldCellType(i, 2 * thirdH - 1, CellType.B);
			game.putFieldCellType(i, 2 * thirdH, CellType.B);
		}
    	int thirdW = (game.fieldWidth + 1) / 3 / 2 * 2;
		for (int j = 1; j < game.fieldHeight - 1; j++) {
			game.putFieldCellType(thirdW - 1, j, CellType.B);
			game.putFieldCellType(thirdW, j, CellType.B);
			game.putFieldCellType(2 * thirdW - 1, j, CellType.B);
			game.putFieldCellType(2 * thirdW, j, CellType.B);
		}

		for (int m = 1; m < 3; m++) {
			for (int n = 1; n < 3; n++) {
				game.putFieldCellType(m * thirdW,     n * thirdH,     CellType.C);
				game.putFieldCellType(m * thirdW - 1, n * thirdH - 1, CellType.C);
				game.putFieldCellType(m * thirdW - 1, n * thirdH,     CellType.C);
				game.putFieldCellType(m * thirdW,     n * thirdH - 1, CellType.C);
			}
		}
		
		synchronized (controlledPlayers) {
			for (int i = 0; i < game.getPlayersCount(); i++) {
				controlledPlayers.put(i, null);
			}
		}
	}

    private static class TheState {
    	@Expose
    	private final Game game;
    	@Expose
    	private final int activePlayerIndex;

    	public TheState(Game game, int activePlayerIndex) {
			this.game = game;
			this.activePlayerIndex = activePlayerIndex;
		}
    	
    	public synchronized String toJson() {
    		return GlobalServices.getGson().toJson(this);
    	}
    }
    
    @Override
    public void gameStateUpdated(Game state) {
    	synchronized (this) {
    		List<Session> sessionsToRemove = new ArrayList<>();

    		for (Session s : sessions) {
    			int playerIndex = -1;
    			for (int i = 0; i < game.getPlayersCount(); i++) {
    	    		if (s == controlledPlayers.get(i)) playerIndex = i;
    	    	}
    			
	    		try {
					s.getRemote().sendString(new TheState(game, playerIndex).toJson());
				} catch (Exception e) {
					sessionsToRemove.add(s);
					e.printStackTrace();
				}
    			
    		}
    		
	    	
    		sessions.removeAll(sessionsToRemove);
    	}
    }
    
    @OnWebSocketConnect
    public void connected(Session session) {
    	System.out.println("Session connected: " + session.getRemoteAddress());
        sessions.add(session);
        if (game.getState() == State.NEW) {
        	game.start();
        }
        
        int selectedPlayer = -1;
		synchronized (controlledPlayers) {
	        for (Integer playerIndex : controlledPlayers.keySet()) {
	        	if (controlledPlayers.get(playerIndex) == null) {
	        		controlledPlayers.put(playerIndex, session);
	        		selectedPlayer = playerIndex;
	        		break;
	        	}
	        }
		}
		
    	synchronized (this) {
			try {
				session.getRemote().sendString(new TheState(game, selectedPlayer).toJson());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
    	System.out.println("Session disconnected: " + session.getRemoteAddress());
        sessions.remove(session);
		synchronized (controlledPlayers) {
	        for (Integer playerIndex : controlledPlayers.keySet()) {
	        	if (controlledPlayers.get(playerIndex) == session) {
	        		controlledPlayers.put(playerIndex, null);
	        		break;
	        	}
	        }
		}
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
		synchronized (controlledPlayers) {
			for (Integer playerIndex : controlledPlayers.keySet()) {
				if (controlledPlayers.get(playerIndex) == session) {
					ClientState cs = ClientState.fromJson(message);
					game.setDebugData(playerIndex, cs.getDebugData());
	        		game.setPlayerKeys(playerIndex, cs.getKeys());
	        	}
	        }
		}
    }

}
