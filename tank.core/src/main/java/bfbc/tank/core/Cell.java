package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.DeltaXY;

public class Cell implements Box {

	private Game game;
	
	@Expose
	private CellType type;

	private int i, j;
	
	@Override
	public double getLeft() {
		return (i - 0.5) * game.cellSize;
	}

	@Override
	public double getTop() {
		return (j - 0.5) * game.cellSize;
	}

	@Override
	public double getRight() {
		return (i + 0.5) * game.cellSize;
	}

	@Override
	public double getBottom() {
		return (j + 0.5) * game.cellSize;
	}

	@Override
	public void move(DeltaXY delta) {
		throw new RuntimeException("Unsupported");

	}
	
	public Cell(Game game, int i, int j) {
		this.game = game;
		this.i = i;
		this.j = j;
		type = CellType.E;
	}
	
	public void setType(CellType type) {
		this.type = type;
	}
	
	public CellType getType() {
		return type;
	}

	@Override
	public boolean isActive() {
		return type == CellType.C ||
		       type == CellType.B;
	}
}
