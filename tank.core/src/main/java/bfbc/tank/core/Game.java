package bfbc.tank.core;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.BoxConstructionCollider.CollisionFriendship;

public class Game extends Thread implements MissileCrashListener {
	
	public static double MODEL_TICK = 1.0 / 120;	// 2 * 60FPS
	public static double FRONTEND_TICK = 1.0 / 30;	// 30FPS
	public static int FRONTEND_DELAY = (int)(1000 * FRONTEND_TICK);
	
	@Expose
	public final int playersCount = 2;
	@Expose
	public final int fieldWidth = 56;
	@Expose
	public final int fieldHeight = 52;
	@Expose
	public final double cellSize = 11;
	
	public static interface StateUpdateHandler {
		void gameStateUpdated(Game state);
	}
	
	private StateUpdateHandler stateUpdateHandler;
	
	private BoxConstructionCollider<Box> collider = new BoxConstructionCollider<>();
	private FieldBoxConstruction fieldBoxConstruction = new FieldBoxConstruction();
	
	@Expose
	private volatile Player[] players;
	
	@Expose
	private volatile int[] frags;
	
	@Expose
	private volatile DebugData[] debugData;
	
	@Expose
	private List<List<Missile>> missiles = new ArrayList<>();

	@Expose
	private volatile Cell[] field = new Cell[fieldWidth * fieldHeight];
	
	private double time;
	private double deltaTime() {
		return (double)System.currentTimeMillis() / 1000 - time;
	}
	private void updateTime(int ticks) {
		time += ticks * MODEL_TICK;
	}
	
	int findPlayerId(Player player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] == player) return i;
		}
		return -1;
	}
	
	private void createPlayer(int index) {
		switch (index) {
		case 0:
			if (players[0] != null) {
				collider.removeAgent(players[0]);
			}
			players[0] = new Player(this, collider, this, Direction.DOWN, 
                    new PlayerKeys(), 
                    false, cellSize * (fieldWidth - 1) / 2, cellSize * 3.0);
			collider.addAgent(players[0]);
			break;
		case 1:
			if (players[1] != null) {
				collider.removeAgent(players[1]);
			}
			players[1] = new Player(this, collider, this, Direction.UP, 
        			new PlayerKeys(), 
        			false, cellSize * (fieldWidth - 1) / 2, cellSize * (fieldHeight - 1 - 3.0));
			collider.addAgent(players[1]);
			break;
			
		}
	}
	
	public Game(StateUpdateHandler stateUpdateHandler) {
		this.stateUpdateHandler = stateUpdateHandler;

		// Saving the current time
		time = (double)System.currentTimeMillis() / 1000;

		// Initializing the field
		for (int j = 0; j < fieldHeight; j++) {
			for (int i = 0; i < fieldWidth; i++) {
				field[j * fieldWidth + i] = new Cell(this, i, j);
				fieldBoxConstruction.add(field[j * fieldWidth + i]);
			}
		}
		collider.addAgent(fieldBoxConstruction);
		
		players = new Player[playersCount];
		frags = new int[playersCount];
		debugData = new DebugData[playersCount];
		
		createPlayer(0);
		createPlayer(1);
		
		collider.setFriendship(new CollisionFriendship<Box>() {
			@Override
			public boolean canCollide(BoxConstruction<Box> con1, BoxConstruction<Box> con2) {
				Player p = null;
				Missile m = null;
				if ((con1 instanceof Player) && (con2 instanceof Missile)) {
					p = (Player) con1;
					m = (Missile) con2;
				} else if ((con2 instanceof Player) && (con1 instanceof Missile)) {
					p = (Player) con2;
					m = (Missile) con1;
				}
				
				if (p != null && m != null) {
					// Missile can't hit the player who has launched it
					return players[m.getOwnerPlayerId()] != p;
				} else {
					// Everything else can collide
					return true;
				}
			}
		});
		
		// Creating lists for player missiles
		for (int i = 0; i < players.length; i++) {
			missiles.add(new ArrayList<>());
		}
	}
	
	public synchronized void frameStep() {
		for (int i = 0; i < players.length; i++) {
			players[i].frameStep();
		}
		for (List<Missile> mm : missiles) {	// TODO Concurrent error!!!!
			Missile[] missileClone = mm.toArray(new Missile[] {});
			for (Missile m : missileClone) {
				m.frameStep();
			}
		}
	}
	
	@Override
	public void run() {
		while (true) {

			double dt = deltaTime();

			int ticks = (int) (dt / MODEL_TICK);
			
			for(int t = 0; t < ticks; t++) {
				frameStep();
			}
			
			stateUpdateHandler.gameStateUpdated(this);
			
			updateTime(ticks);

			try {
				Thread.sleep(FRONTEND_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public void setDebugData(int playerIndex, DebugData debugData) {
		this.debugData[playerIndex] = debugData;
	}
	
	public synchronized void setPlayerKeys(int playerIndex, PlayerKeys playerCommand) {
		this.players[playerIndex].setActiveCommand(playerCommand);
	}
	
	public synchronized String toJson() {
		return GlobalServices.getGson().toJson(this);
	}
	
	public synchronized void putFieldCellType(int x, int y, CellType cell) {
		this.field[y * fieldWidth + x].setType(cell);
		
	}
	
	public synchronized CellType getFieldCellType(int x, int y) {
		return this.field[y * fieldWidth + x].getType();
	}
	public synchronized int getPlayersCount() {
		return playersCount;
	}
	
	synchronized Missile createMissile(Player p, double posX, double posY, double angle, double velocity) {
		int index = findPlayerId(p);
		if (missiles.get(index).isEmpty()) {
			Missile newMissile = new Missile(this, this, collider, findPlayerId(p), posX, posY, angle, velocity);
			missiles.get(index).add(newMissile);
			collider.addAgent(newMissile);
			return newMissile;
		} else {
			return null;
		}
	}
	
	@Override
	public void missileCrashed(Missile missile, BoxConstruction<?> target) {
		if (target instanceof Player) {
			Player p = (Player)target;
			if (players[missile.getOwnerPlayerId()] != p) {
				int id = findPlayerId(p);
				int myId = missile.getOwnerPlayerId();
				System.out.println("Player " + (id + 1) + " is killed by player " + (myId + 1));
				frags[myId] ++;
				createPlayer(id);
			}
		}
		
		missiles.get(missile.getOwnerPlayerId()).remove(missile);
		collider.removeAgent(missile);
	}
}
