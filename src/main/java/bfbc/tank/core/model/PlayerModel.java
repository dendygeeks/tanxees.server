package bfbc.tank.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.Appearance;
import bfbc.tank.core.api.DebugData;
import bfbc.tank.core.api.Missile;
import bfbc.tank.core.api.Player;
import bfbc.tank.core.api.PlayerUnit;
import bfbc.tank.core.api.UnitType;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.model.DebugDataModel;

public class PlayerModel implements Player {
	
	@Expose
	private PlayerUnitModel unit;

	@Expose
	private UnitType unitType;
	
	@Expose
	private Appearance appearance;

	@Expose
	private int frags;

	@Expose
	private DebugDataModel debugData;

	@Expose
	private List<Missile> missiles = new ArrayList<>();

	public PlayerModel(UnitType unitType, Appearance appearance, int frags) {
		this.unitType = unitType;
		this.appearance = appearance;
		this.frags = frags;
	}
	
	@Override
	public UnitType getUnitType() {
		return unitType;
	}

	@Override
	public PlayerUnit getUnit() {
		return unit;
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

	@Override
	public boolean ownsMissile(Missile m) {
		return missiles.contains(m);
	}
	
	public void setFrags(int frags) {
		this.frags = frags;
	}

	public void setPlayerUnitModel(PlayerUnitModel playerUnitModel) {
		this.unit = playerUnitModel;
	}
	
	public void setDebugData(DebugDataModel debugData) {
		this.debugData = debugData;
	}
	
}
