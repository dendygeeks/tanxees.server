package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.DeltaXY;

public class Cell implements Box {

	@Expose
	private CellType type;
	
	private static final double SIZE = 22;

	private int i, j;
	
	@Override
	public double getLeft() {
		return i * SIZE;
	}

	@Override
	public double getTop() {
		return j * SIZE;
	}

	@Override
	public double getRight() {
		return (i + 1) * SIZE;
	}

	@Override
	public double getBottom() {
		return (j + 1) * SIZE;
	}

	@Override
	public void move(DeltaXY delta) {
		throw new RuntimeException("Unsupported");

	}
	
	public Cell(int i, int j) {
		this.i = i;
		this.j = j;
		type = CellType.EMPTY;
	}
	
	public void setType(CellType type) {
		this.type = type;
	}
	
	public CellType getType() {
		return type;
	}

	@Override
	public boolean isActive() {
		return type == CellType.CONCRETE;
	}
}
