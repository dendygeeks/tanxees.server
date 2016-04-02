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

@WebSocket(maxTextMessageSize = 1024 * 1024 * 10, maxBinaryMessageSize = 1024 * 1024 * 100)
public class PlayerWebSocket implements StateUpdateHandler {

	// Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    private Map<String, Session> controlledPlayers = new HashMap<>();
    
    private Game game;
    
	private String[] playerIds = new String[] { "player1", "player2", "bot1", "bot2" };
    
    public PlayerWebSocket() {
    	// Walls
    	CellType __ = CellType.EMPTY, _C = CellType.CONCRETE, _B = CellType.BRICKS;
    	CellType[] map = new CellType[] {
   			__, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __,
   			__, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, _C, _C, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, _C, _C, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __,
   			__, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __,
   			_B, _B, __, __, _B, _B, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, _B, _B, __, __, _B, _B,
   			_C, _C, __, __, _B, _B, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, _B, _B, __, __, _C, _C,
   			__, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __,
   			__, __, __, __, __, __, __, __, __, __, _B, _B, _B, _B, _B, _B, __, __, __, __, __, __, __, __, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, _B, _B, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, _B, _B, __, __, _B, _B, __, __, __, _B, _B, _B, _B, __, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, __, __, __, __, __, __, __, __, __, _B, __, __, _B, __, __, __, __, __, __, __, __, __, __, __,
    		__, __, __, __, __, __, __, __, __, __, __, _B, __, __, _B, __, __, __, __, __, __, __, __, __, __, __
    	};
    	
    	HashMap<String, PointIJ> spawnPoints = new HashMap<>();
    	spawnPoints.put(playerIds[0], new PointIJ(19, 50));
    	spawnPoints.put(playerIds[1], new PointIJ(33, 50));
    	spawnPoints.put(playerIds[2], new PointIJ(2, 2));
    	spawnPoints.put(playerIds[3], new PointIJ(50, 2));
    	
    	HashMap<String, Direction> spawnDirs = new HashMap<>();
    	spawnDirs.put(playerIds[0], Direction.UP);
    	spawnDirs.put(playerIds[1], Direction.UP);
    	spawnDirs.put(playerIds[2], Direction.DOWN);
    	spawnDirs.put(playerIds[3], Direction.DOWN);
    	
    	game = new Game(this, 26, 26, map, playerIds, spawnPoints, spawnDirs);
		
		synchronized (controlledPlayers) {
			for (String id : playerIds) {
				controlledPlayers.put(id, null);
			}
		}
	}

    private static class TheState {
    	@Expose
    	private final Game game;
    	@Expose
    	private final String activePlayerId;

    	public TheState(Game game, String activePlayerId) {
			this.game = game;
			this.activePlayerId = activePlayerId;
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
    			String controlledPlayerId = null;
    			for (String id : playerIds) {
    	    		if (s == controlledPlayers.get(id)) controlledPlayerId = id;
    	    	}
    			
	    		try {
					s.getRemote().sendString(new TheState(game, controlledPlayerId).toJson());
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
        
        String playerId = session.getUpgradeRequest().getParameterMap().get("playerId").get(0);
        
        if (controlledPlayers.get(playerId) == null) {
        	controlledPlayers.put(playerId, session);
        } else {
        	System.err.println("Can't connect client to player id \"" + playerId + "\". It's occupied.");
        }
        
        /*int selectedPlayer = -1;
		synchronized (controlledPlayers) {
			ArrayList<String> plrs = new ArrayList<>(controlledPlayers.keySet());
			for (int playerIndex = plrs.size() - 1; playerIndex >= 0; playerIndex--) {
	        	if (controlledPlayers.get(playerIndex) == null) {
	        		controlledPlayers.put(playerIndex, session);
	        		selectedPlayer = playerIndex;
	        		break;
	        	}
			}
		}*/
		
    	synchronized (this) {
			try {
				session.getRemote().sendString(new TheState(game, playerId).toJson());
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
	        for (String playerId : controlledPlayers.keySet()) {
	        	if (controlledPlayers.get(playerId) == session) {
	        		controlledPlayers.put(playerId, null);
	        		break;
	        	}
	        }
		}
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
		synchronized (controlledPlayers) {
	        for (String playerId : controlledPlayers.keySet()) {
				if (controlledPlayers.get(playerId) == session) {
					ClientState cs = ClientState.fromJson(message);
					game.setDebugData(playerId, cs.getDebugData());
	        		game.setPlayerKeys(playerId, cs.getKeys());
	        	}
	        }
		}
    }

}
