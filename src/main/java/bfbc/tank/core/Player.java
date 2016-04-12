package bfbc.tank.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import bfbc.tank.core.PlayerUnit.SpawnConfig;
import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstructionCollider;

public class Player {
	enum Appearance {

		GREEN("green"), YELLOW("yellow"), GRAY("gray");

		static class TypeAdapter extends com.google.gson.TypeAdapter<Appearance> {
			public Appearance read(JsonReader reader) throws IOException {
				// TODO It can't read
				return null;
			}

			public void write(JsonWriter writer, Appearance value) throws IOException {
				if (value == null) {
					writer.nullValue();
					return;
				}
				String name = value.id;
				writer.value(name);
			}
		}

		public final String id;

		Appearance(String id) {
			this.id = id;
		}
	}
	
	enum UnitType {

		SMALL("small", 20/*22*/, 27), MEDIUM("medium", 34, 34);

		static class TypeAdapter extends com.google.gson.TypeAdapter<UnitType> {
			public UnitType read(JsonReader reader) throws IOException {
				// TODO It can't read
				return null;
			}

			public void write(JsonWriter writer, UnitType value) throws IOException {
				if (value == null) {
					writer.nullValue();
					return;
				}
				String name = value.id;
				writer.value(name);
			}
		}

		public final String id;
		public final double sizeW, sizeL;

		UnitType(String id, double sizeW, double sizeL) {
			this.id = id;
			this.sizeW = sizeW;
			this.sizeL = sizeL;
		}
	}
	
	private MissileCrashListener missileCrashListener;
	private BoxConstructionCollider<Box> collider;
	
	@Expose
	private PlayerUnit unit;

	@Expose
	private UnitType unitType;
	
	@Expose
	private Appearance appearance;

	@Expose
	private int frags;

	@Expose
	private DebugData debugData;

	@Expose
	private List<Missile> missiles = new ArrayList<>();

/*	private PointIJ spawnPoint;
	private Direction spawnDir;*/

	PlayerUnit getUnit() {
		return unit;
	}


	public Player(MissileCrashListener missileCrashListener, BoxConstructionCollider<Box> collider, Appearance appearance, UnitType unitType) {
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
			m.frameStep();
		}
	}

	public void setDebugData(DebugData debugData) {
		this.debugData = debugData;
	}

	public void setPlayerKeys(PlayerKeys playerKeys) {
		unit.setActiveCommand(playerKeys);
	}

	public boolean ownsMissile(Missile missile) {
		return missiles.contains(missile);
	}
	
	void removeMissile(Missile m) {
		collider.removeAgent(m);
		missiles.remove(m);
	}

	public void incrementFrags() {
		frags++;
	}

	public void respawnUnit(double cellSize, PlayerUnit.SpawnConfig spawnConfig) {
		if (unit != null) {
			collider.removeAgent(unit);
		}

		unit = new PlayerUnit(this, 
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

	void addMissile(Missile newMissile) {
		missiles.add(newMissile);
		collider.addAgent(newMissile);
	}
	
	int getMissilesCount() {
		return missiles.size();
	}
}
