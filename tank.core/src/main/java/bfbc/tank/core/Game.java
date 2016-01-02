package bfbc.tank.core;

import java.util.ArrayList;
import java.util.List;

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
	public final int playersCount = 2;
	@Expose
	public final int fieldWidth = 29;
	@Expose
	public final int fieldHeight = 27;
	@Expose
	public final double cellSize = 22;
	
	public static interface StateUpdateHandler {
		void gameStateUpdated(Game state);
	}
	
	private StateUpdateHandler stateUpdateHandler;
	
	private BoxConstructionCollider<Box> collider = new BoxConstructionCollider<>();
	private FieldBoxConstruction fieldBoxConstruction = new FieldBoxConstruction();
	
	@Expose
	private volatile Player[] players;
	
	@Expose
	private List<List<Missile>> missiles = new ArrayList<>();

	@Expose
	private volatile Cell[] field = new Cell[fieldWidth * fieldHeight];
	
	private double time;
	private double deltaTime() {
		return (double)System.currentTimeMillis() / 1000 - time;
	}
	private void updateTime(int ticks) {
		time += ticks * MODEL_TICK;
	}
	
	int findPlayerId(Player player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] == player) return i;
		}
		return -1;
	}
	
	public Game(StateUpdateHandler stateUpdateHandler) {
		this.stateUpdateHandler = stateUpdateHandler;
		for (int j = 0; j < fieldHeight; j++) {
			for (int i = 0; i < fieldWidth; i++) {
				field[j * fieldWidth + i] = new Cell(this, i, j);
				fieldBoxConstruction.add(field[j * fieldWidth + i]);
			}
		}
		
		collider.addAgent(fieldBoxConstruction);
		
		time = (double)System.currentTimeMillis() / 1000;
		players = new Player[2];
		players[0] = new Player(this, collider, Direction.RIGHT, 
		                    new PlayerCommand(), 
		                    false, 75, 250, 0);
		players[1] = new Player(this, collider, Direction.LEFT, 
                			new PlayerCommand(), 
                			false, 200, 250, 180);
		collider.addAgent(players[0]);
		collider.addAgent(players[1]);
		
		collider.setFriendship(new CollisionFriendship<Box>() {
			@Override
			public boolean canCollide(BoxConstruction<Box> con1, BoxConstruction<Box> con2) {
				Player p = null;
				Missile m = null;
				if ((con1 instanceof Player) && (con2 instanceof Missile)) {
					p = (Player) con1;
					m = (Missile) con2;
				} else if ((con2 instanceof Player) && (con1 instanceof Missile)) {
					p = (Player) con2;
					m = (Missile) con1;
				}
				
				if (p != null && m != null) {
					// Missile can't hit the player who has launched it
					return m.getOwnerPlayer() != p;
				} else {
					// Everything else can collide
					return true;
				}
			}
		});
		
		// Creating lists for player missiles
		for (int i = 0; i < players.length; i++) {
			missiles.add(new ArrayList<>());
		}
	}
	
	public synchronized void frameStep() {
		for (int i = 0; i < players.length; i++) {
			players[i].frameStep();
		}
		for (List<Missile> mm : missiles) {	// TODO Concurrent error!!!!
			Missile[] missileClone = mm.toArray(new Missile[] {});
			for (Missile m : missileClone) {
				m.frameStep();
			}
		}
	}
	
	@Override
	public void run() {
		while (true) {

			double dt = deltaTime();

			int ticks = (int) (dt / MODEL_TICK);
			
			for(int t = 0; t < ticks; t++) {
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
	
	public synchronized void setPlayerCommands(int playerIndex, PlayerCommand playerCommand) {
		this.players[playerIndex].setActiveCommand(playerCommand);
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
	public synchronized int getPlayersCount() {
		return playersCount;
	}
	
	synchronized Missile createMissile(Player p, double posX, double posY, double angle, double velocity) {
		int index = findPlayerId(p);
		if (missiles.get(index).isEmpty()) {
			Missile newMissile = new Missile(this, this, collider, p, posX, posY, angle, velocity);
			missiles.get(index).add(newMissile);
			collider.addAgent(newMissile);
			return newMissile;
		} else {
			return null;
		}
	}
	
	@Override
	public void missileCrashed(Missile missile, BoxConstruction<?> target) {
		if (target == players[0]) {
			System.out.println("Player 1 eliminated");
		} else if (target == players[1]) {
			System.out.println("Player 2 eliminated");
		}
		
		missiles.get(findPlayerId(missile.getOwnerPlayer())).remove(missile);
		collider.removeAgent(missile);
	}
}
