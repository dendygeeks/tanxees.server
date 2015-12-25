package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstructionCollider;

public class Game extends Thread {
	
	@Expose
	public final int fieldWidth = 28;
	@Expose
	public final int fieldHeight = 26;
	
	public static interface StateUpdateHandler {
		void update(Game state);
	}
	
	private StateUpdateHandler stateUpdateHandler;
	
	private BoxConstructionCollider<Box> collider = new BoxConstructionCollider<>();
	private FieldBoxConstruction fieldBoxConstruction = new FieldBoxConstruction();
	
	@Expose
	private volatile Player player;
	
	@Expose
	private volatile Cell[] field = new Cell[fieldWidth * fieldHeight];
	
	private double time;
	private double deltaTime() {
		return (double)System.currentTimeMillis() / 1000 - time;
	}
	private void updateTime() {
		time = (double)System.currentTimeMillis() / 1000;
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
		player = new Player(collider, Direction.RIGHT, 
		                    new PlayerCommand(false, false, false, false), 
		                    false, 75, 25, 0);
		collider.addAgent(player);
	}
	
	public void frameStep(double dt) {
		player.frameStep(dt);
	}
	
	@Override
	public void run() {
		while (true) {

			double dt = deltaTime();

			frameStep(dt);
			
			stateUpdateHandler.update(this);
			
			updateTime();

			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public void setPlayerCommands(PlayerCommand playerCommand) {
		this.player.setActiveCommand(playerCommand);
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

}
