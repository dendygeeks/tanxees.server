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
    
    private Game game;
    
    
    
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
    	
    	game = new Game(this, 26, 26, map, 4, new PointIJ[] {
    		new PointIJ(19, 50),
    		new PointIJ(33, 50),
    		new PointIJ(2, 2),
    		//new PointIJ(26, 2),
    		new PointIJ(50, 2)
    	}, new Direction[] { Direction.UP, Direction.UP, Direction.DOWN, Direction.DOWN/*, Direction.DOWN */});
		
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
			ArrayList<Integer> plrs = new ArrayList<>(controlledPlayers.keySet());
			for (int playerIndex = plrs.size() - 1; playerIndex >= 0; playerIndex--) {
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
