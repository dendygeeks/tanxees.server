package dendygeeks.tanxees.server;
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

import dendygeeks.tanxees.api.java.interfaces.Appearance;
import dendygeeks.tanxees.api.java.interfaces.CellType;
import dendygeeks.tanxees.api.java.interfaces.UnitType;
import dendygeeks.tanxees.api.java.model.ClientStateModel;
import dendygeeks.tanxees.api.java.model.DebugDataModel;
import dendygeeks.tanxees.server.controllers.ServerGameController;
import dendygeeks.tanxees.server.controllers.TheStateController;
import dendygeeks.tanxees.server.controllers.ServerGameController.StateUpdateHandler;

@WebSocket(maxTextMessageSize = 1024 * 1024 * 10, maxBinaryMessageSize = 1024 * 1024 * 100)
public class PlayerServerWebSocket implements StateUpdateHandler {

	// Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    private Map<String, Session> controlledPlayers = new HashMap<>();
    
    private ServerGameController game;
    
    private static final String PLAYER_ID_PLAYER1 = "player1";
    private static final String PLAYER_ID_PLAYER2 = "player2";
    private static final String PLAYER_ID_BOT1 = "bot1";
    private static final String PLAYER_ID_BOT2 = "bot2";
    private static final String PLAYER_ID_BOT3 = "bot3";

    //private String[] playerIds = new String[] { PLAYER_ID_PLAYER1, PLAYER_ID_PLAYER2, PLAYER_ID_BOT1, PLAYER_ID_BOT2 };
    private String[] playerIds = new String[] { PLAYER_ID_PLAYER1, PLAYER_ID_BOT1, PLAYER_ID_BOT2, PLAYER_ID_BOT3 };

    public PlayerServerWebSocket() {
    	// Walls
    	CellType __ = CellType.EMPTY, _C = CellType.CONCRETE, _B = CellType.BRICKS, DB = CellType.DARK_BRICKS;
    	/*CellType[] map = new CellType[] {
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
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, _B, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, _B, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
   			__, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, _B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, _B, _B, __, __, _B, _B, __, __, __, _B, _B, _B, _B, __, __, __, _B, _B, __, __, _B, _B, __, __,
    		__, __, __, __, __, __, __, __, __, __, __, _B, DB, DB, _B, __, __, __, __, __, __, __, __, __, __, __,
    		__, __, __, __, __, __, __, __, __, __, __, _B, DB, DB, _B, __, __, __, __, __, __, __, __, __, __, __
    	};
    	
    	HashMap<String, SpawnConfig> spawnConfigs = new HashMap<>();
    	spawnConfigs.put(PLAYER_ID_PLAYER1, new SpawnConfig(new PointIJ(19, 50), Direction.UP));
    	spawnConfigs.put(PLAYER_ID_PLAYER2, new SpawnConfig(new PointIJ(33, 50), Direction.UP));
    	spawnConfigs.put(PLAYER_ID_BOT1, new SpawnConfig(new PointIJ(2, 2), Direction.UP));
    	spawnConfigs.put(PLAYER_ID_BOT2, new SpawnConfig(new PointIJ(50, 2), Direction.UP));
    	
    	HashMap<String, Appearance> appearances = new HashMap<>();
    	appearances.put(PLAYER_ID_PLAYER1, Appearance.GREEN);
    	appearances.put(PLAYER_ID_PLAYER2, Appearance.YELLOW);
    	appearances.put(PLAYER_ID_BOT1, Appearance.GRAY);
    	appearances.put(PLAYER_ID_BOT2, Appearance.GRAY);
    	
    	HashMap<String, UnitType> unitTypes = new HashMap<>();
    	unitTypes.put(PLAYER_ID_PLAYER1, UnitType.MEDIUM);
    	unitTypes.put(PLAYER_ID_PLAYER2, UnitType.SMALL);
    	unitTypes.put(PLAYER_ID_BOT1, UnitType.MEDIUM);
    	unitTypes.put(PLAYER_ID_BOT2, UnitType.MEDIUM);
    	*/
    	
    	CellType[] map = new CellType[] {
       			_B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, _B, _B,
       			_B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, __, _B, _B,
       			__, __, __, __, _B, _B, __, __, __, _B, _B, _B, _C, _C, _B, _B, _B, __, __, __, __, __, __, __, __, __,
       			__, __, __, __, _B, _B, __, __, __, _B, _B, _B, _C, _C, _B, _B, _B, __, __, __, __, __, __, __, __, __,
       			__, __, __, __, _C, _C, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _C, _C, __, __, __, __,
       			__, __, __, __, _C, _C, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, _C, _C, __, __, __, __,
       			__, __, __, __, __, __, _C, _C, _B, _B, __, __, _B, _B, __, __, _C, _C, __, __, __, __, __, __, __, __,
       			__, __, __, __, __, __, _C, _C, _B, _B, __, _B, _B, _B, _B, __, _C, _C, __, __, __, __, __, __, __, __,
       			__, __, __, __, _B, _B, _B, _B, _C, _C, _B, _B, _B, _B, _B, _B, _C, _C, _C, _C, _B, _B, __, __, __, __,
       			__, __, _B, _B, _B, _B, _B, _B, _C, _C, _B, _B, _B, _B, _B, _B, _C, _C, _C, _C, _B, _B, _B, _B, __, __,
       			__, __, _B, _B, __, __, __, __, _B, _B, __, __, __, __, _B, _C, _B, _B, __, __, __, __, _B, _B, __, __,
       			__, __, _B, _B, __, __, __, _B, _B, _B, __, __, DB, DB, _C, __, _B, _B, _B, __, __, __, _B, _B, __, __,
       			__, __, _C, _C, __, __, _B, _B, _B, _B, __, DB, _C, _C, DB, __, _B, _B, _B, _B, __, __, _C, _C, __, __,
       			__, __, _C, _C, __, __, _B, _B, _B, _B, __, DB, _C, _C, DB, __, _B, _B, _B, _B, __, __, _C, _C, __, __,
       			__, __, _B, _B, __, __, __, _B, _B, _B, __, _C, DB, DB, __, __, _B, _B, _B, __, __, __, _B, _B, __, __,
       			__, __, _B, _B, __, __, __, __, _B, _B, _C, __, __, __, __, __, _B, _B, __, __, __, __, _B, _B, __, __,
       			__, __, _B, _B, _B, _B, _C, _C, _C, _C, _B, _B, _B, _B, _B, _B, _C, _C, _B, _B, _B, _B, _B, _B, __, __,
       			__, __, __, __, _B, _B, _C, _C, _C, _C, _B, _B, _B, _B, _B, _B, _C, _C, _B, _B, _B, _B, __, __, __, __,
       			__, __, __, __, __, __, __, __, _C, _C, __, _B, _B, _B, _B, __, _B, _B, _C, _C, __, __, __, __, __, __,
       			__, __, __, __, __, __, __, __, _C, _C, __, __, _B, _B, __, __, _B, _B, _C, _C, __, __, __, __, __, __,
       			__, __, __, __, _C, _C, __, __, __, __, __, __, __, __, __, __, __, __, __, __, _C, _C, __, __, __, __,
        		__, __, __, __, _C, _C, __, __, __, __, __, __, __, __, __, __, __, __, __, __, _C, _C, __, __, __, __,
        		__, __, __, __, _B, _B, __, __, __, _B, _B, _B, _C, _C, _B, _B, _B, __, __, __, _B, _B, __, __, __, __,
        		__, __, __, __, _B, _B, __, __, __, _B, _B, _B, _C, _C, _B, _B, _B, __, __, __, _B, _B, __, __, __, __,
        		_B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B,
        		_B, _B, __, __, _B, _B, __, __, __, __, __, __, __, __, __, __, __, __, __, __, _B, _B, __, __, _B, _B
        	};
    	
    	HashMap<String, SpawnConfig> spawnConfigs = new HashMap<>();
    	spawnConfigs.put(PLAYER_ID_PLAYER1, new SpawnConfig(new PointIJ(26, 50), Direction.UP));
    	spawnConfigs.put(PLAYER_ID_BOT1, new SpawnConfig(new PointIJ(26, 2), Direction.UP));
    	spawnConfigs.put(PLAYER_ID_BOT2, new SpawnConfig(new PointIJ(2, 26), Direction.RIGHT));
    	spawnConfigs.put(PLAYER_ID_BOT3, new SpawnConfig(new PointIJ(50, 26), Direction.LEFT));
    	
    	HashMap<String, Appearance> appearances = new HashMap<>();
    	appearances.put(PLAYER_ID_PLAYER1, Appearance.GREEN);
    	appearances.put(PLAYER_ID_BOT1, Appearance.GRAY);
    	appearances.put(PLAYER_ID_BOT2, Appearance.GRAY);
    	appearances.put(PLAYER_ID_BOT3, Appearance.GRAY);
    	
    	HashMap<String, UnitType> unitTypes = new HashMap<>();
    	unitTypes.put(PLAYER_ID_PLAYER1, UnitType.MEDIUM);
    	unitTypes.put(PLAYER_ID_BOT1, UnitType.MEDIUM);
    	unitTypes.put(PLAYER_ID_BOT2, UnitType.MEDIUM);
    	unitTypes.put(PLAYER_ID_BOT3, UnitType.MEDIUM);
    	
    	game = new ServerGameController(this, 26, 26, map, playerIds, appearances, unitTypes, spawnConfigs, 27, 51);
		
		synchronized (controlledPlayers) {
			for (String id : playerIds) {
				controlledPlayers.put(id, null);
			}
		}
	}

    @Override
    public void gameStateUpdated(ServerGameController state) {
    	synchronized (this) {
    		List<Session> sessionsToRemove = new ArrayList<>();

    		for (Session s : sessions) {
    			String controlledPlayerId = null;
    			for (String id : playerIds) {
    	    		if (s == controlledPlayers.get(id)) controlledPlayerId = id;
    	    	}
    			
	    		try {
					s.getRemote().sendString(new TheStateController(game, controlledPlayerId).toJson());
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
				session.getRemote().sendString(new TheStateController(game, playerId).toJson());
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
					ClientStateModel<DebugDataModel> cs = ClientStateModel.fromJson(DebugDataModel.class, message);
					game.setDebugData(playerId, cs.getDebugData());
	        		game.setPlayerKeys(playerId, cs.getKeys());
	        	}
	        }
		}
    }

}
