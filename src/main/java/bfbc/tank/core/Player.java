package bfbc.tank.core;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class Player {
	private Game game;
	
	@Expose
	private PlayerUnit unit;
	
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

	void createTank() {
		if (unit != null) {
			game.getCollider().removeAgent(unit);
		}
		
		unit = new PlayerUnit(game, game.getCollider(), game, spawnDir, 
                new PlayerKeys(), 
                false, 
                game.cellSize * (spawnPoint.i + 0.5), 
                game.cellSize * (spawnPoint.j + 0.5)
        );
		
        game.getCollider().addAgent(unit);
	}

	public Player(Game game, PointIJ spawnPoint, Direction spawnDir) {
		this.game = game;
		this.frags = 0;
		this.debugData = null;
		this.missiles = new ArrayList<>();
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
	
	Missile createMissile(String myId, double posX, double posY, double angle, double velocity) {
		if (missiles.isEmpty()) {
			Missile newMissile = new Missile(game, game, game.getCollider(), myId, posX, posY, angle, velocity);
			missiles.add(newMissile);
			game.getCollider().addAgent(newMissile);
			return newMissile;
		} else {
			return null;
		}
	}
	
	void destroyMissile(Missile m) {
		missiles.remove(m);
		game.getCollider().removeAgent(m);
	}

	public void incrementFrags() {
		frags ++;
	}
}
