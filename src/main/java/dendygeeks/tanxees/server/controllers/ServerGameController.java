package dendygeeks.tanxees.server.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.annotations.Expose;

import dendygeeks.tanxees.api.java.interfaces.Appearance;
import dendygeeks.tanxees.api.java.interfaces.CellType;
import dendygeeks.tanxees.api.java.interfaces.UnitType;
import dendygeeks.tanxees.api.java.model.CellModel;
import dendygeeks.tanxees.api.java.model.DebugDataModel;
import dendygeeks.tanxees.api.java.model.GameModel;
import dendygeeks.tanxees.api.java.model.PlayerKeysModel;
import dendygeeks.tanxees.server.GameSetup;
import dendygeeks.tanxees.server.MissileCrashListener;
import dendygeeks.tanxees.server.SpawnConfig;
import dendygeeks.tanxees.server.mechanics.BoxConstruction;
import dendygeeks.tanxees.server.mechanics.BoxConstructionCollider;
import dendygeeks.tanxees.server.mechanics.BoxConstructionCollider.CollisionFriendship;

public class ServerGameController extends Thread implements MissileCrashListener {
	
	public static final double MODEL_TICK = 1.0 / 60;	// 2 * 60FPS
	public static final double FRONTEND_TICK = 1.0 / 25;	// 30FPS
	public static final int FRONTEND_DELAY = (int)(1000 * FRONTEND_TICK);
			
	public static final int CELL_SIZE = 11;
	
	private GameSetup gameSetup; 
	private GameModel gameModel;
	
	public GameModel getGameModel() {
		return gameModel;
	}
	
	public GameSetup getGameSetup() {
		return gameSetup;
	}
	
	public static interface StateUpdateHandler {
		void gameStateUpdated(ServerGameController state);
	}
	
	private StateUpdateHandler stateUpdateHandler;
	
	
	private BoxConstructionCollider collider = new BoxConstructionCollider();
	//private ArrayList<CellBoxConstruction> cells = new ArrayList<>();
	
	@Expose
	private volatile HashMap<String, ServerPlayerController> playerControllers;
	@Expose
	private volatile ServerFlagController flagController;
	
	@Expose
	private volatile ServerCellController[] fieldCellControllers;// = new Cell[fieldWidth * fieldHeight];
	
	private double time;
	private double deltaTime() {
		return (double)System.currentTimeMillis() / 1000 - time;
	}
	private void updateTime(int ticks) {
		time += ticks * MODEL_TICK;
	}
	
	public String findPlayerId(ServerPlayerController player) {
		for (Entry<String, ServerPlayerController> e : playerControllers.entrySet()) {
			if (e.getValue() == player) return e.getKey();
		}
		return null;
	}

	String findPlayerIdByTank(ServerPlayerUnitController tank) {
		for (Entry<String, ServerPlayerController> e : playerControllers.entrySet()) {
			if (e.getValue().getUnit() == tank) return e.getKey();
		}
		return null;
	}
	
	ServerPlayerController findMissileOwner(ServerMissileController missile) {
		for (ServerPlayerController p : playerControllers.values()) {
			if (p.ownsMissile(missile)) return p;
		}
		return null;
	}

	BoxConstructionCollider getCollider() {
		return collider;
	}
	
	private void createPlayerUnit(String id) {
		playerControllers.get(id).respawnUnit();
	}
	
	public ServerGameController(StateUpdateHandler stateUpdateHandler, int mapWidth, int mapHeight, GameSetup gameSetup, int flagI, int flagJ) {
		
		this.gameSetup = gameSetup;
		
		if (gameSetup.getMap() == null) throw new IllegalArgumentException("Map shouldn't be null");
		if (gameSetup.getMap().length != mapWidth * mapHeight) throw new IllegalArgumentException("Invalid map size");
		
		int fieldWidth = mapWidth * 2 + 2;
		int fieldHeight = mapHeight * 2 + 2;

		fieldCellControllers = new ServerCellController[fieldWidth * fieldHeight];
		
		// Initializing the field
		for (int j = 0; j < fieldHeight; j++) {
			for (int i = 0; i < fieldWidth; i++) {
				fieldCellControllers[j * fieldWidth + i] = new ServerCellController(this, i, j);
				//cells.add(field[j * fieldWidth + i]);
				collider.addAgent(fieldCellControllers[j * fieldWidth + i]);
			}
		}

		// Extracting cell models from cell controllers
		CellModel[] fieldCellModels = new CellModel[fieldCellControllers.length];
		for (int i = 0; i < fieldCellModels.length; i++) {
			fieldCellModels[i] = fieldCellControllers[i].getCellModel();
		}
		
		// No flag for demo
		//flagController = new ServerFlagController(CELL_SIZE * (flagI - 0.5), CELL_SIZE * (flagJ - 0.5));
		//collider.addAgent(flagController);
		flagController = null;
		

		gameModel = new GameModel(
				fieldWidth,
				fieldHeight, 
				false, 
				null, // [No flag for demo] flagController.getFlagModel(), 
				fieldCellModels,
				CELL_SIZE
		);
		
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
				fieldCellControllers[i + j * fieldWidth].getCellModel().setType(gameSetup.getMap()[((i-1)/2) + ((j-1)/2)*mapWidth]);
			}
		}

		if (gameSetup.getSpawnConfigs() == null) throw new IllegalArgumentException("spawnConfigs shouldn't be null");
		
		// TODO Validation
		//if (playersCount != spawnPoints.length) throw new IllegalArgumentException("Players and spawn points count differ");
		//if (playersCount != spawnDirs.length) throw new IllegalArgumentException("Players and spawn dirs count differ");
		//this.playersCount = playersCount;
		
		this.stateUpdateHandler = stateUpdateHandler;

		// Saving the current time
		time = (double)System.currentTimeMillis() / 1000;

		playerControllers = new HashMap<String, ServerPlayerController>();
		
		for (String id : gameSetup.getPlayerIds()) {
			ServerPlayerController newPlayerController = new ServerPlayerController(this, collider, gameSetup.getUnitTypes().get(id), gameSetup.getAppearances().get(id), this, time, this.gameSetup.getSpawnConfigs().get(id));
			playerControllers.put(id, newPlayerController);
			gameModel.getPlayers().put(id, newPlayerController.getPlayerModel());
			createPlayerUnit(id);
		}
		
		collider.setFriendship(new CollisionFriendship() {
			@Override
			public boolean canCollide(BoxConstruction<?> con1, BoxConstruction<?> con2) {
				ServerPlayerUnitController t = null;
				ServerMissileController m = null;
				if ((con1 instanceof ServerPlayerUnitController) && (con2 instanceof ServerMissileController)) {
					t = (ServerPlayerUnitController) con1;
					m = (ServerMissileController) con2;
				} else if ((con2 instanceof ServerPlayerUnitController) && (con1 instanceof ServerMissileController)) {
					t = (ServerPlayerUnitController) con2;
					m = (ServerMissileController) con1;
				}
				
				if (t != null && m != null) {
					// Missile can't hit the player who has launched it
					if (!t.isInvincible()) {
						return findMissileOwner(m).getUnit() != t;
					} else {
						// Invincible units can't collide with missiles
						return false;
					}
				} else {
					// Everything else can collide
					return true;
				}
			}
		});
	}
	
	public synchronized void frameStep() {
		HashMap<String, ServerPlayerController> playersClone = new HashMap<>(playerControllers);
		for (ServerPlayerController p : playersClone.values()) {
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
	
	public void setDebugData(String id, DebugDataModel debugData) {
		playerControllers.get(id).setDebugData(debugData);
	}
	
	public synchronized void setPlayerKeys(String id, PlayerKeysModel playerCommand) {
		playerControllers.get(id).setPlayerKeys(playerCommand);
	}
	
	public synchronized void putFieldCellType(int x, int y, CellType cell) {
		this.fieldCellControllers[y * gameModel.getFieldWidth() + x].getCellModel().setType(cell);
		
	}
	
	public synchronized CellType getFieldCellType(int x, int y) {
		return this.fieldCellControllers[y * gameModel.getFieldWidth() + x].getCellModel().getType();
	}

	private boolean removeBricksAfterCrash(double i, double j, double upI, double upJ, double rightI, double rightJ) {
		// Checking boundaries
		if (i < 0 || i >= gameModel.getFieldWidth() || j < 0 || j >= gameModel.getFieldHeight()) return false;
		// Checking if this cell is occupied by a brick
		if (!fieldCellControllers[(int)i + (int)j * gameModel.getFieldWidth()].getCellModel().getType().isBrick()) return false;
		
		// Checking the closer cell
		double i2 = i - rightI, j2 = j - rightJ;
		// Checking boundaries for the closer cell
		if (i2 < 0 || i2 >= gameModel.getFieldWidth() || j2 < 0 || j2 >= gameModel.getFieldHeight()) return false;
		// Checking if this cell is empty
		if (fieldCellControllers[(int)i2 + (int)j2 * gameModel.getFieldWidth()].getCellModel().getType().isWall()) return false;
		
		// If we are still here, destroying the brick in (x, y)
		fieldCellControllers[(int)i + (int)j * gameModel.getFieldWidth()].getCellModel().setType(CellType.EMPTY);
		
		return true;
	}
	
	@Override
	public void missileCrashed(ServerMissileController missile, Collection<BoxConstruction<?>> targets) {
		ServerPlayerController missileOwner = findMissileOwner(missile);
		for (BoxConstruction<?> target : targets) {
			if (target instanceof ServerPlayerUnitController) {
				ServerPlayerUnitController t = (ServerPlayerUnitController)target;
				if (missileOwner.getUnit() != t) {
					String id = findPlayerIdByTank(t);
					String myId = findPlayerId(missileOwner);
					System.out.println("Player " + id + " is killed by player " + myId);
					((ServerPlayerController)playerControllers.get(myId)).incrementFrags();
					createPlayerUnit(id);
				}
			} else if (target instanceof ServerMissileController) {
				// If to missiles collide they both are destroyed
				ServerMissileController targetMissile = (ServerMissileController)target;
				ServerPlayerController targetOwner = findMissileOwner(targetMissile);
				targetOwner.removeMissile(targetMissile);
			} else if (target instanceof ServerCellController) {
				// A missile hit a brick wall
				ServerCellController c = (ServerCellController)target;
				if (c.getCellModel().getType().isBrick()) {
					
					// Destroying the brick instantly ^_^
					c.getCellModel().setType(CellType.EMPTY);
				
					// Searching for adjacent bricks to destroy
		
					// Assuming the missile is moving "right", calculting "up" vector
					double rightI = Math.cos(missile.getMissileModel().getAngle() * Math.PI / 180), rightJ = Math.sin(missile.getMissileModel().getAngle() * Math.PI / 180);
					double upI = rightJ, upJ = -rightI;
					
					// Checking if the missile did hit "above" or "below" the brick center
					double fromBrickToMissileX = missile.getMissileModel().getPosX() - c.getX(),
					       fromBrickToMissileY = missile.getMissileModel().getPosY() - c.getY();
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
			} else if (target instanceof ServerFlagController) {
				((ServerFlagController) target).getFlagModel().setCrashed(true);
				gameModel.setOver(true);
				
			}
		}
		missileOwner.removeMissile(missile);
	}
}
