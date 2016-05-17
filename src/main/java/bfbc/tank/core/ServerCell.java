package bfbc.tank.core;

import java.util.Iterator;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.Cell;
import bfbc.tank.core.api.CellType;
import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.DeltaAngle;
import bfbc.tank.core.mechanics.DeltaXY;

public class ServerCell implements Cell, Box, BoxConstruction<ServerCell> {

	private ServerGame game;
	
	@Expose
	private CellType type;

	private int i, j;
	
	@Override
	public double getExcentricity() {
		return 0;
	}
	
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
	
	public ServerCell(ServerGame game, int i, int j, CellType type) {
		this.game = game;
		this.i = i;
		this.j = j;
		this.type = type; //CellType.E;
	}

	public ServerCell(ServerGame game, int i, int j) {
		this(game, i, j, CellType.EMPTY);
	}

	public void setType(CellType type) {
		this.type = type;
	}
	
	public CellType getType() {
		return type;
	}

	@Override
	public boolean isActive() {
		return type.isWall();
	}
	
	@Override
	public Iterator<ServerCell> iterator() {
		Iterator<ServerCell> res = new Iterator<ServerCell>() {
			
			private boolean hasNext = true;
			
			@Override
			public ServerCell next() {
				hasNext = false;
				return ServerCell.this;
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

	@Override
	public void rotate(DeltaAngle delta) {
		throw new RuntimeException("Not supported");
	}
}
