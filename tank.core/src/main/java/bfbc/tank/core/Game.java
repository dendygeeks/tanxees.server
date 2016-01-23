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
	public final int fieldWidth;// = 28 * 2;
	@Expose
	public final int fieldHeight;// = 26 * 2;
	@Expose
	public final double cellSize = 11;
	
	public static interface StateUpdateHandler {
		void gameStateUpdated(Game state);
	}
	
	private StateUpdateHandler stateUpdateHandler;
	
	private BoxConstructionCollider<Box> collider = new BoxConstructionCollider<>();
	//private ArrayList<CellBoxConstruction> cells = new ArrayList<>();
	
	@Expose
	private volatile Player[] players;
	
	@Expose
	private volatile int[] frags;
	
	@Expose
	private volatile DebugData[] debugData;
	
	@Expose
	private List<List<Missile>> missiles = new ArrayList<>();

	@Expose
	private volatile Cell[] field;// = new Cell[fieldWidth * fieldHeight];
	
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
	
	public Game(StateUpdateHandler stateUpdateHandler, int mapWidth, int mapHeight, CellType[] map) {
		if (map == null) throw new IllegalArgumentException("Map shouldn't be null");
		if (map.length != mapWidth * mapHeight) throw new IllegalArgumentException("Invalid map size");
		this.fieldWidth = mapWidth * 2 + 2;
		this.fieldHeight = mapHeight * 2 + 2;
		field = new Cell[fieldWidth * fieldHeight];
		
		// Initializing the field
		for (int j = 0; j < fieldHeight; j++) {
			for (int i = 0; i < fieldWidth; i++) {
				field[j * fieldWidth + i] = new Cell(this, i, j);
				//cells.add(field[j * fieldWidth + i]);
				collider.addAgent(field[j * fieldWidth + i]);
			}
		}

		// Outer walls
		for (int i = 0; i < fieldHeight - 1; i++) {
    		putFieldCellType(0, i, CellType.C);
    	}
    	for (int i = 1; i < fieldHeight; i++) {
    		putFieldCellType(fieldWidth - 1, i, CellType.C);
    	}
    	for (int i = 1; i < fieldWidth; i++) {
    		putFieldCellType(i, 0, CellType.C);
    	}
    	for (int i = 0; i < fieldWidth - 1; i++) {
    		putFieldCellType(i, fieldHeight - 1, CellType.C);
    	}

    	// Map
		for (int i = 1; i < fieldWidth - 1; i++) {
			for (int j = 1; j < fieldHeight - 1; j++) {
				field[i + j * fieldWidth].setType(map[((i-1)/2) + ((j-1)/2)*mapWidth]);
			}
		}

		
		
		this.stateUpdateHandler = stateUpdateHandler;

		// Saving the current time
		time = (double)System.currentTimeMillis() / 1000;

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
			Missile newMissile = new Missile(this, this, collider, index, posX, posY, angle, velocity);
			missiles.get(index).add(newMissile);
			collider.addAgent(newMissile);
			return newMissile;
		} else {
			return null;
		}
	}
	
	private boolean removeBricksAfterCrash(double i, double j, double upI, double upJ, double rightI, double rightJ) {
		// Checking boundaries
		if (i < 0 || i >= fieldWidth || j < 0 || j >= fieldHeight) return false;
		// Checking if this cell is occupied by a brick
		if (field[(int)i + (int)j * fieldWidth].getType() != CellType.B) return false;
		
		// Checking the closer cell
		double i2 = i - rightI, j2 = j - rightJ;
		// Checking boundaries for the closer cell
		if (i2 < 0 || i2 >= fieldWidth || j2 < 0 || j2 >= fieldHeight) return false;
		// Checking if this cell is empty
		if (field[(int)i2 + (int)j2 * fieldWidth].getType().isOccupied()) return false;
		
		// If we are still here, destroying the brick in (x, y)
		field[(int)i + (int)j * fieldWidth].setType(CellType.E);
		
		return true;
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
		} else if (target instanceof Cell) {
			// A missile hit a brick wall
			Cell c = (Cell)target;
			if (c.getType() == CellType.B) {
				
				// Destroying the brick instantly ^_^
				c.setType(CellType.E);
			
				// Searching for adjacent bricks to destroy
	
				// Assuming the missile is moving "right", calculting "up" vector
				double rightI = Math.cos(missile.angle * Math.PI / 180), rightJ = Math.sin(missile.angle * Math.PI / 180);
				double upI = rightJ, upJ = -rightI;
				
				// Checking if the missile did hit "above" or "below" the brick center
				double fromBrickToMissileX = missile.posX - c.getX(),
				       fromBrickToMissileY = missile.posY - c.getY();
				boolean missileHitsAboveCenter = (upJ * fromBrickToMissileY + upI * fromBrickToMissileX) > 0;
				
				if (!missileHitsAboveCenter) { upJ *= -1; upI *= -1; }	// Now it is above ;)
				
				// walking up
				double i = c.getI() + 0.5, j = c.getJ() + 0.5;	// we add 0.5 to fix rounding 0.9999...
				for (int k = 1; k < 3; k++) {
					i += upI; j += upJ;
					if (!removeBricksAfterCrash(i, j, upI, upJ, rightI, rightJ)) break;
				}
	
				// walking down
				i = c.getI() + 0.5; j = c.getJ() + 0.5;	// we add 0.5 to fix rounding 0.9999...
				for (int k = 1; k < 2; k++) {
					i -= upI; j -= upJ;
					if (!removeBricksAfterCrash(i, j, upI, upJ, rightI, rightJ)) break;
				}
			}

		}
		
		missiles.get(missile.getOwnerPlayerId()).remove(missile);
		collider.removeAgent(missile);
	}
}
