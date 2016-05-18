package bfbc.tank.core.controllers;

import java.util.ArrayList;
import java.util.List;

import bfbc.tank.api.interfaces.Appearance;
import bfbc.tank.api.interfaces.UnitType;
import bfbc.tank.api.model.PlayerModel;
import bfbc.tank.core.MissileCrashListener;
import bfbc.tank.core.PlayerKeys;
import bfbc.tank.core.SpawnConfig;
import bfbc.tank.core.mechanics.BoxConstructionCollider;

public class ServerPlayerController {

	private MissileCrashListener missileCrashListener;
	private BoxConstructionCollider collider;
	
	private PlayerModel playerModel;
	
	private ServerPlayerUnitController unit;
	private ServerDebugDataController debugData;
	private List<ServerMissileController> missileControllers = new ArrayList<>();

	public PlayerModel getPlayerModel() {
		return playerModel;
	}
	
/*	private PointIJ spawnPoint;
	private Direction spawnDir;*/

	public ServerPlayerController(MissileCrashListener missileCrashListener, BoxConstructionCollider collider, UnitType unitType, Appearance appearance, double cellSize, SpawnConfig spawnConfig) {
		this.playerModel = new PlayerModel(unitType, appearance, 0);
		this.missileCrashListener = missileCrashListener;
		this.collider = collider;
		this.debugData = null;
		
		this.missileControllers = new ArrayList<>();
		
		debugData = new ServerDebugDataController();
		playerModel.setDebugData(debugData.getDebugDataModel());
		
		unit = new ServerPlayerUnitController(this, 
				playerModel.getUnitType().sizeW,
				playerModel.getUnitType().sizeL,
				cellSize, 
				collider, 
				missileCrashListener, 
				spawnConfig, 
				new PlayerKeys(), 
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

	public void setDebugData(ServerDebugDataController debugData) {
		this.debugData = debugData;
	}

	public void setPlayerKeys(PlayerKeys playerKeys) {
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
	}
