package bfbc.tank.core;

import java.util.HashMap;
import java.util.Map;

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
	private volatile HashMap<String, Player> players;
	
	@Expose
	private volatile Cell[] field;// = new Cell[fieldWidth * fieldHeight];
	
	private double time;
	private double deltaTime() {
		return (double)System.currentTimeMillis() / 1000 - time;
	}
	private void updateTime(int ticks) {
		time += ticks * MODEL_TICK;
	}
	
	String findPlayerId(Player player) {
		for (Map.Entry<String, Player> e : players.entrySet()) {
			if (e.getValue() == player) return e.getKey();
		}
		return null;
	}

	String findPlayerIdByTank(PlayerUnit tank) {
		for (Map.Entry<String, Player> e : players.entrySet()) {
			if (e.getValue().getUnit() == tank) return e.getKey();
		}
		return null;
	}
	
	Player findMissileOwner(Missile missile) {
		for (Player p : players.values()) {
			if (p.ownsMissile(missile)) return p;
		}
		return null;
	}

	BoxConstructionCollider<Box> getCollider() {
		return collider;
	}
	
	private void createPlayerUnit(String id) {
		players.get(id).respawnUnit(cellSize);
	}
	
	public Game(StateUpdateHandler stateUpdateHandler, int mapWidth, int mapHeight, CellType[] map, String[] playerIds, HashMap<String, Player.Appearance> appearances, HashMap<String, Player.UnitType> unitTypes, HashMap<String, PointIJ> spawnPoints, HashMap<String, Direction> spawnDirs) {
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
    		putFieldCellType(0, i, CellType.DARK_CONCRETE);
    	}
    	for (int i = 1; i < fieldHeight; i++) {
    		putFieldCellType(fieldWidth - 1, i, CellType.DARK_CONCRETE);
    	}
    	for (int i = 1; i < fieldWidth; i++) {
    		putFieldCellType(i, 0, CellType.DARK_CONCRETE);
    	}
    	for (int i = 0; i < fieldWidth - 1; i++) {
    		putFieldCellType(i, fieldHeight - 1, CellType.DARK_CONCRETE);
    	}

    	// Map
		for (int i = 1; i < fieldWidth - 1; i++) {
			for (int j = 1; j < fieldHeight - 1; j++) {
				field[i + j * fieldWidth].setType(map[((i-1)/2) + ((j-1)/2)*mapWidth]);
			}
		}

		if (spawnPoints == null) throw new IllegalArgumentException("spawnPoints shouldn't be null");
		if (spawnDirs == null) throw new IllegalArgumentException("spawnDirs shouldn't be null");
		
		// TODO Validation
		//if (playersCount != spawnPoints.length) throw new IllegalArgumentException("Players and spawn points count differ");
		//if (playersCount != spawnDirs.length) throw new IllegalArgumentException("Players and spawn dirs count differ");
		//this.playersCount = playersCount;
		
		this.stateUpdateHandler = stateUpdateHandler;

		// Saving the current time
		time = (double)System.currentTimeMillis() / 1000;

		players = new HashMap<String, Player>();

		
		for (String id : playerIds) {
			players.put(id, new Player(this, collider, appearances.get(id), unitTypes.get(id), spawnPoints.get(id), spawnDirs.get(id)));
			createPlayerUnit(id);
		}
		
		collider.setFriendship(new CollisionFriendship<Box>() {
			@Override
			public boolean canCollide(BoxConstruction<Box> con1, BoxConstruction<Box> con2) {
				PlayerUnit t = null;
				Missile m = null;
				if ((con1 instanceof PlayerUnit) && (con2 instanceof Missile)) {
					t = (PlayerUnit) con1;
					m = (Missile) con2;
				} else if ((con2 instanceof PlayerUnit) && (con1 instanceof Missile)) {
					t = (PlayerUnit) con2;
					m = (Missile) con1;
				}
				
				if (t != null && m != null) {
					// Missile can't hit the player who has launched it
					return findMissileOwner(m).getUnit() != t;
				} else {
					// Everything else can collide
					return true;
				}
			}
		});

	}
	
	public synchronized void frameStep() {
		HashMap<String, Player> playersClone = new HashMap<>(players);
		for (Player p : playersClone.values()) {
			p.frameStep();
		}
	}
	
	@Override
	public void run() {
		while (true) {

			double dt = deltaTime();

			int ticks = (int) (dt / MODEL_TICK);
			
			for (int t = 0; t < ticks; t++) {
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
	
	public void setDebugData(String id, DebugData debugData) {
		this.players.get(id).setDebugData(debugData);
	}
	
	public synchronized void setPlayerKeys(String id, PlayerKeys playerCommand) {
		this.players.get(id).setPlayerKeys(playerCommand);
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
	
	private boolean removeBricksAfterCrash(double i, double j, double upI, double upJ, double rightI, double rightJ) {
		// Checking boundaries
		if (i < 0 || i >= fieldWidth || j < 0 || j >= fieldHeight) return false;
		// Checking if this cell is occupied by a brick
		if (field[(int)i + (int)j * fieldWidth].getType() != CellType.BRICKS) return false;
		
		// Checking the closer cell
		double i2 = i - rightI, j2 = j - rightJ;
		// Checking boundaries for the closer cell
		if (i2 < 0 || i2 >= fieldWidth || j2 < 0 || j2 >= fieldHeight) return false;
		// Checking if this cell is empty
		if (field[(int)i2 + (int)j2 * fieldWidth].getType().isWall()) return false;
		
		// If we are still here, destroying the brick in (x, y)
		field[(int)i + (int)j * fieldWidth].setType(CellType.EMPTY);
		
		return true;
	}
	
	@Override
	public void missileCrashed(Missile missile, BoxConstruction<?> target) {
		Player missileOwner = findMissileOwner(missile);
		if (target instanceof PlayerUnit) {
			PlayerUnit t = (PlayerUnit)target;
			if (missileOwner.getUnit() != t) {
				String id = findPlayerIdByTank(t);
				String myId = findPlayerId(missileOwner);
				System.out.println("Player " + id + " is killed by player " + myId);
				players.get(myId).incrementFrags();
				createPlayerUnit(id);
			}
		} else if (target instanceof Missile) {
			// If to missiles collide they both are destroyed
			Missile targetMissile = (Missile)target;
			Player targetOwner = findMissileOwner(targetMissile);
			targetOwner.removeMissile(targetMissile);
		} else if (target instanceof Cell) {
			// A missile hit a brick wall
			Cell c = (Cell)target;
			if (c.getType() == CellType.BRICKS) {
				
				// Destroying the brick instantly ^_^
				c.setType(CellType.EMPTY);
			
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
		
		missileOwner.removeMissile(missile);
	}
}
