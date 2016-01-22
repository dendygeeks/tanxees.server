package bfbc.tank.core;

import java.util.Iterator;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.DeltaXY;

public class Cell implements Box, BoxConstruction<Cell> {

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
	
	public double getX() {
		return i * game.cellSize;
	}
	public double getY() {
		return j * game.cellSize;
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
	
	@Override
	public Iterator<Cell> iterator() {
		Iterator<Cell> res = new Iterator<Cell>() {
			
			private boolean hasNext = true;
			
			@Override
			public Cell next() {
				hasNext = false;
				return Cell.this;
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
		return res;
	}
	
	public int getI() {
		return i;
	}
	
	public int getJ() {
		return j;
	}
}
