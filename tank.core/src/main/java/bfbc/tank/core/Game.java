package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstructionCollider;

public class Game extends Thread {
	
	public static double TICK = 1.0 / 120;	// 2 * 60FPS
	public static double FRONTEND_TICK = 1.0 / 30;	// 30FPS
	public static int FRONTEND_DELAY = (int)(1000 * FRONTEND_TICK);
	
	@Expose
	public final int fieldWidth = 29;
	@Expose
	public final int fieldHeight = 27;
	
	public static interface StateUpdateHandler {
		void update(Game state);
	}
	
	private StateUpdateHandler stateUpdateHandler;
	
	private BoxConstructionCollider<Box> collider = new BoxConstructionCollider<>();
	private FieldBoxConstruction fieldBoxConstruction = new FieldBoxConstruction();
	
	@Expose
	private volatile Player[] players;
	
	@Expose
	private volatile Cell[] field = new Cell[fieldWidth * fieldHeight];
	
	private double time;
	private double deltaTime() {
		return (double)System.currentTimeMillis() / 1000 - time;
	}
	private void updateTime(int ticks) {
		time += ticks * TICK;
	}
	
	public Game(StateUpdateHandler stateUpdateHandler) {
		this.stateUpdateHandler = stateUpdateHandler;
		for (int j = 0; j < fieldHeight; j++) {
			for (int i = 0; i < fieldWidth; i++) {
				field[j * fieldWidth + i] = new Cell(i, j);
				fieldBoxConstruction.add(field[j * fieldWidth + i]);
			}
		}
		
		collider.addAgent(fieldBoxConstruction);
		
		time = (double)System.currentTimeMillis() / 1000;
		players = new Player[2];
		players[0] = new Player(collider, Direction.RIGHT, 
		                    new PlayerCommand(false, false, false, false), 
		                    false, 75, 25, 0);
		players[1] = new Player(collider, Direction.LEFT, 
                new PlayerCommand(false, false, false, false), 
                false, 200, 25, 0);
		collider.addAgent(players[0]);
		collider.addAgent(players[1]);
	}
	
	public void frameStep() {
		for (int i = 0; i < players.length; i++) {
			players[i].frameStep();
		}
	}
	
	@Override
	public void run() {
		while (true) {

			double dt = deltaTime();

			int ticks = (int) (dt / TICK);
			
			for(int t = 0; t < ticks; t++) {
				frameStep();
			}
			
			stateUpdateHandler.update(this);
			
			updateTime(ticks);

			try {
				Thread.sleep(FRONTEND_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public void setPlayerCommands(int playerIndex, PlayerCommand playerCommand) {
		this.players[playerIndex].setActiveCommand(playerCommand);
	}
	
	public String toJson() {
		return GlobalServices.getGson().toJson(this);
	}
	
	public void putFieldCellType(int x, int y, CellType cell) {
		this.field[y * fieldWidth + x].setType(cell);
		
	}
	
	public CellType getFieldCellType(int x, int y) {
		return this.field[y * fieldWidth + x].getType();
	}
	public int getPlayersCount() {
		return 2;
	}

}
