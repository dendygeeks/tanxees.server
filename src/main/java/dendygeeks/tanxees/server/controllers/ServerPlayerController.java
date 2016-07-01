package dendygeeks.tanxees.server.controllers;

import java.util.ArrayList;
import java.util.List;

import dendygeeks.tanxees.api.java.interfaces.Appearance;
import dendygeeks.tanxees.api.java.interfaces.CellType;
import dendygeeks.tanxees.api.java.interfaces.UnitType;
import dendygeeks.tanxees.api.java.model.DebugDataModel;
import dendygeeks.tanxees.api.java.model.PlayerKeysModel;
import dendygeeks.tanxees.api.java.model.PlayerModel;
import dendygeeks.tanxees.server.MissileCrashListener;
import dendygeeks.tanxees.server.SpawnConfig;
import dendygeeks.tanxees.server.mechanics.BoxConstructionCollider;

public class ServerPlayerController {
	private ServerGameController gameController;

	private MissileCrashListener missileCrashListener;
	private BoxConstructionCollider collider;
	
	private PlayerModel playerModel;
	
	private ServerPlayerUnitController unit;
	private DebugDataModel debugData;
	private List<ServerMissileController> missileControllers = new ArrayList<>();

	public PlayerModel getPlayerModel() {
		return playerModel;
	}
	
/*	private PointIJ spawnPoint;
	private Direction spawnDir;*/

	public ServerPlayerController(MissileCrashListener missileCrashListener, BoxConstructionCollider collider, UnitType unitType, Appearance appearance, ServerGameController gameController, SpawnConfig spawnConfig) {
		this.playerModel = new PlayerModel(unitType, appearance, 0);
		this.missileCrashListener = missileCrashListener;
		this.collider = collider;
		this.debugData = null;
		this.gameController = gameController;
		
		this.missileControllers = new ArrayList<>();
		
		debugData = new DebugDataModel("");
		playerModel.setDebugData(debugData);
		
		unit = new ServerPlayerUnitController(this, 
				playerModel.getUnitType().sizeW,
				playerModel.getUnitType().sizeL,
				playerModel.getUnitType().mass,
				playerModel.getUnitType().midship,
				playerModel.getUnitType().forwardPower,
				playerModel.getUnitType().backwardPower,
				ServerGameController.CELL_SIZE, 
				collider, 
				missileCrashListener, 
				spawnConfig, 
				new PlayerKeysModel(), 
				false
				/*, cellSize * (spawnPoint.i + 0.5), 
				cellSize * (spawnPoint.j + 0.5)*/
			);
		playerModel.setPlayerUnitModel(unit.getPlayerUnitModel());
		collider.addAgent(unit);
	}

	public void frameStep() {
		unit.frameStep();
		List<ServerMissileController> missilesClone = new ArrayList<>(missileControllers);
		for (ServerMissileController m : missilesClone) {
			((ServerMissileController)m).frameStep();
		}
	}

	public void setDebugData(DebugDataModel debugData) {
		this.debugData = debugData;
	}

	public void setPlayerKeys(PlayerKeysModel playerKeys) {
		unit.setActiveCommand(playerKeys);
	}

	public boolean ownsMissile(ServerMissileController missile) {
		return missileControllers.contains(missile);
	}
	
	void removeMissile(ServerMissileController m) {
		collider.removeAgent(m);
		missileControllers.remove(m);
		playerModel.getMissiles().remove(m.getMissileModel());
	}

	public void incrementFrags() {
		playerModel.setFrags(playerModel.getFrags() + 1);
	}

	public void respawnUnit() {
		/*if (unit != null) {
			collider.removeAgent(unit);
		}*/
		
		this.unit.respawnUnit();
		
		//collider.addAgent(unit);
	}

	void addMissile(ServerMissileController newMissile) {
		missileControllers.add(newMissile);
		playerModel.getMissiles().add(newMissile.getMissileModel());
		collider.addAgent(newMissile);
	}
	
	int getMissilesCount() {
		return missileControllers.size();
	}
	
	public ServerPlayerUnitController getUnit() {
		return unit;
	}
	
	public CellType getFieldCellType(int x, int y) {
		return gameController.getFieldCellType(x, y);
	}
}
