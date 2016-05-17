package bfbc.tank.core;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.DebugData;
import bfbc.tank.core.api.Missile;
import bfbc.tank.core.api.Player;
import bfbc.tank.core.mechanics.BoxConstructionCollider;

public class ServerPlayer implements Player {

	private MissileCrashListener missileCrashListener;
	private BoxConstructionCollider collider;
	
	@Expose
	private ServerPlayerUnit unit;

	@Expose
	private UnitType unitType;
	
	@Expose
	private Appearance appearance;

	@Expose
	private int frags;

	@Expose
	private ServerDebugData debugData;

	@Expose
	private List<Missile> missiles = new ArrayList<>();

/*	private PointIJ spawnPoint;
	private Direction spawnDir;*/

	public ServerPlayerUnit getUnit() {
		return unit;
	}


	public ServerPlayer(MissileCrashListener missileCrashListener, BoxConstructionCollider collider, Appearance appearance, UnitType unitType) {
		this.missileCrashListener = missileCrashListener;
		this.collider = collider;
		this.frags = 0;
		this.debugData = null;
		this.missiles = new ArrayList<>();
		this.appearance = appearance;
		this.unitType = unitType;
	}

	public void frameStep() {
		unit.frameStep();
		List<Missile> missilesClone = new ArrayList<>(missiles);
		for (Missile m : missilesClone) {
			((ServerMissile)m).frameStep();
		}
	}

	public void setDebugData(ServerDebugData debugData) {
		this.debugData = debugData;
	}

	public void setPlayerKeys(PlayerKeys playerKeys) {
		unit.setActiveCommand(playerKeys);
	}

	public boolean ownsMissile(ServerMissile missile) {
		return missiles.contains(missile);
	}
	
	void removeMissile(ServerMissile m) {
		collider.removeAgent(m);
		missiles.remove(m);
	}

	public void incrementFrags() {
		frags++;
	}

	public void respawnUnit(double cellSize, ServerPlayerUnit.SpawnConfig spawnConfig) {
		if (unit != null) {
			collider.removeAgent(unit);
		}

		unit = new ServerPlayerUnit(this, 
			unitType.sizeW,
			unitType.sizeL,
			cellSize, 
			collider, 
			missileCrashListener, 
			spawnConfig, 
			new PlayerKeys(), 
			false
			/*, cellSize * (spawnPoint.i + 0.5), 
			cellSize * (spawnPoint.j + 0.5)*/
		);

		collider.addAgent(unit);
	}

	void addMissile(ServerMissile newMissile) {
		missiles.add(newMissile);
		collider.addAgent(newMissile);
	}
	
	int getMissilesCount() {
		return missiles.size();
	}


	@Override
	public UnitType getUnitType() {
		return unitType;
	}


	@Override
	public Appearance getAppearance() {
		return appearance;
	}


	@Override
	public int getFrags() {
		return frags;
	}


	@Override
	public DebugData getDebugData() {
		return debugData;
	}


	@Override
	public List<Missile> getMissiles() {
		return missiles;
	}
}
