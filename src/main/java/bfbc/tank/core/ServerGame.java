package bfbc.tank.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.Cell;
import bfbc.tank.core.api.CellType;
import bfbc.tank.core.api.Flag;
import bfbc.tank.core.api.Game;
import bfbc.tank.core.api.Player;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.BoxConstructionCollider.CollisionFriendship;

public class ServerGame extends Thread implements Game, MissileCrashListener {
	
	public static double MODEL_TICK = 1.0 / 120;	// 2 * 60FPS
	public static double FRONTEND_TICK = 1.0 / 30;	// 30FPS
	public static int FRONTEND_DELAY = (int)(1000 * FRONTEND_TICK);
	
	@Expose
	public final int fieldWidth;// = 28 * 2;
	@Expose
	public final int fieldHeight;// = 26 * 2;
	@Expose
	public final double cellSize = 11;
	
	@Expose
	public boolean isOver = false;
	
	public static interface StateUpdateHandler {
		void gameStateUpdated(ServerGame state);
	}
	
	private StateUpdateHandler stateUpdateHandler;
	
	private HashMap<String, ServerPlayerUnit.SpawnConfig> spawnConfigs; 
	
	private BoxConstructionCollider collider = new BoxConstructionCollider();
	//private ArrayList<CellBoxConstruction> cells = new ArrayList<>();
	
	@Expose
	private volatile HashMap<String, Player> players;
	@Expose
	private volatile ServerFlag flag;
	
	@Expose
	private volatile ServerCell[] field;// = new Cell[fieldWidth * fieldHeight];
	
	private double time;
	private double deltaTime() {
		return (double)System.currentTimeMillis() / 1000 - time;
	}
	private void updateTime(int ticks) {
		time += ticks * MODEL_TICK;
	}
	
	String findPlayerId(ServerPlayer player) {
		for (Entry<String, Player> e : players.entrySet()) {
			if (e.getValue() == player) return e.getKey();
		}
		return null;
	}

	String findPlayerIdByTank(ServerPlayerUnit tank) {
		for (Entry<String, Player> e : players.entrySet()) {
			if (e.getValue().getUnit() == tank) return e.getKey();
		}
		return null;
	}
	
	ServerPlayer findMissileOwner(ServerMissile missile) {
		for (Player p : players.values()) {
			if (((ServerPlayer)p).ownsMissile(missile)) return (ServerPlayer)p;
		}
		return null;
	}

	BoxConstructionCollider getCollider() {
		return collider;
	}
	
	private void createPlayerUnit(String id) {
		((ServerPlayer)players.get(id)).respawnUnit(cellSize, spawnConfigs.get(id));
	}
	
	public ServerGame(StateUpdateHandler stateUpdateHandler, int mapWidth, int mapHeight, CellType[] map, String[] playerIds, HashMap<String, ServerPlayer.Appearance> appearances, HashMap<String, ServerPlayer.UnitType> unitTypes, HashMap<String, ServerPlayerUnit.SpawnConfig> spawnConfigs, int flagI, int flagJ) {
		this.spawnConfigs = new HashMap<>(spawnConfigs);
		
		if (map == null) throw new IllegalArgumentException("Map shouldn't be null");
		if (map.length != mapWidth * mapHeight) throw new IllegalArgumentException("Invalid map size");
		this.fieldWidth = mapWidth * 2 + 2;
		this.fieldHeight = mapHeight * 2 + 2;
		field = new ServerCell[fieldWidth * fieldHeight];
		
		// Initializing the field
		for (int j = 0; j < fieldHeight; j++) {
			for (int i = 0; i < fieldWidth; i++) {
				field[j * fieldWidth + i] = new ServerCell(this, i, j);
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

		if (spawnConfigs == null) throw new IllegalArgumentException("spawnConfigs shouldn't be null");
		
		// TODO Validation
		//if (playersCount != spawnPoints.length) throw new IllegalArgumentException("Players and spawn points count differ");
		//if (playersCount != spawnDirs.length) throw new IllegalArgumentException("Players and spawn dirs count differ");
		//this.playersCount = playersCount;
		
		this.stateUpdateHandler = stateUpdateHandler;

		// Saving the current time
		time = (double)System.currentTimeMillis() / 1000;

		players = new HashMap<String, Player>();
		flag = new ServerFlag(cellSize * flagI, cellSize * flagJ);
		collider.addAgent(flag);
		
		for (String id : playerIds) {
			players.put(id, new ServerPlayer(this, collider, appearances.get(id), unitTypes.get(id)));
			createPlayerUnit(id);
		}
		
		collider.setFriendship(new CollisionFriendship() {
			@Override
			public boolean canCollide(BoxConstruction<?> con1, BoxConstruction<?> con2) {
				ServerPlayerUnit t = null;
				ServerMissile m = null;
				if ((con1 instanceof ServerPlayerUnit) && (con2 instanceof ServerMissile)) {
					t = (ServerPlayerUnit) con1;
					m = (ServerMissile) con2;
				} else if ((con2 instanceof ServerPlayerUnit) && (con1 instanceof ServerMissile)) {
					t = (ServerPlayerUnit) con2;
					m = (ServerMissile) con1;
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
		HashMap<String,Player> playersClone = new HashMap<>(players);
		for (Player p : playersClone.values()) {
			((ServerPlayer)p).frameStep();
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
	
	public void setDebugData(String id, ServerDebugData debugData) {
		((ServerPlayer)players.get(id)).setDebugData(debugData);
	}
	
	public synchronized void setPlayerKeys(String id, PlayerKeys playerCommand) {
		((ServerPlayer)players.get(id)).setPlayerKeys(playerCommand);
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
	
	public boolean isOver() {
		return isOver;
	}
	
	private boolean removeBricksAfterCrash(double i, double j, double upI, double upJ, double rightI, double rightJ) {
		// Checking boundaries
		if (i < 0 || i >= fieldWidth || j < 0 || j >= fieldHeight) return false;
		// Checking if this cell is occupied by a brick
		if (!field[(int)i + (int)j * fieldWidth].getType().isBrick()) return false;
		
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
	public void missileCrashed(ServerMissile missile, Collection<BoxConstruction<?>> targets) {
		ServerPlayer missileOwner = findMissileOwner(missile);
		for (BoxConstruction<?> target : targets) {
			if (target instanceof ServerPlayerUnit) {
				ServerPlayerUnit t = (ServerPlayerUnit)target;
				if (missileOwner.getUnit() != t) {
					String id = findPlayerIdByTank(t);
					String myId = findPlayerId(missileOwner);
					System.out.println("Player " + id + " is killed by player " + myId);
					((ServerPlayer)players.get(myId)).incrementFrags();
					createPlayerUnit(id);
				}
			} else if (target instanceof ServerMissile) {
				// If to missiles collide they both are destroyed
				ServerMissile targetMissile = (ServerMissile)target;
				ServerPlayer targetOwner = findMissileOwner(targetMissile);
				targetOwner.removeMissile(targetMissile);
			} else if (target instanceof ServerCell) {
				// A missile hit a brick wall
				ServerCell c = (ServerCell)target;
				if (c.getType().isBrick()) {
					
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
			} else if (target instanceof ServerFlag) {
				((ServerFlag) target).setCrashed(true);
				isOver = true;
				
			}
		}
		missileOwner.removeMissile(missile);
	}
	@Override
	public double getCellSize() {
		return cellSize;
	}
	@Override
	public int getFieldWidth() {
		return fieldWidth;
	}
	@Override
	public int getFieldHeight() {
		return fieldHeight;
	}
	@Override
	public HashMap<String, Player> getPlayers() {
		return players;
	}
	@Override
	public Flag getFlag() {
		return flag;
	}
	@Override
	public Cell[] getField() {
		return field;
	}
}
