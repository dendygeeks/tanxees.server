package bfbc.tank.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Player implements MissileSpawner {
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

	private Game game;

	@Expose
	private PlayerUnit unit;

	@Expose
	private Appearance appearance;

	@Expose
	private int frags;

	@Expose
	private DebugData debugData;

	@Expose
	private List<Missile> missiles = new ArrayList<>();

	private PointIJ spawnPoint;
	private Direction spawnDir;

	PlayerUnit getUnit() {
		return unit;
	}

	void createUnit() {
		if (unit != null) {
			game.getCollider().removeAgent(unit);
		}

		unit = new PlayerUnit(this, game.cellSize, game.getCollider(), game, spawnDir, new PlayerKeys(), false,
				game.cellSize * (spawnPoint.i + 0.5), game.cellSize * (spawnPoint.j + 0.5));

		game.getCollider().addAgent(unit);
	}

	public Player(Game game, Appearance appearance, PointIJ spawnPoint, Direction spawnDir) {
		this.game = game;
		this.frags = 0;
		this.debugData = null;
		this.missiles = new ArrayList<>();
		this.appearance = appearance;
		this.spawnPoint = spawnPoint;
		this.spawnDir = spawnDir;
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

	public Missile spawnMissile(PlayerUnit t, double posX, double posY, double angle, double velocity) {
		if (missiles.isEmpty()) {
			Missile newMissile = new Missile(game, game.getCollider(), posX, posY, angle, velocity);
			missiles.add(newMissile);
			game.getCollider().addAgent(newMissile);
			return newMissile;
		} else {
			return null;
		}
	}

	public boolean ownsMissile(Missile missile) {
		return missiles.contains(missile);
	}
	
	void destroyMissile(Missile m) {
		missiles.remove(m);
		game.getCollider().removeAgent(m);
	}

	public void incrementFrags() {
		frags++;
	}
}
