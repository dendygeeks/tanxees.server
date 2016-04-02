package bfbc.tank.core;

import java.io.IOException;
import java.util.Iterator;

import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.DeltaAngle;
import bfbc.tank.core.mechanics.DeltaXY;

public class Cell implements Box, BoxConstruction<Cell> {

	static class TypeAdapter extends com.google.gson.TypeAdapter<Cell> {
		public Cell read(JsonReader reader) throws IOException {
			// TODO It can't read
			/*if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}
			String name = reader.nextString();
			return Cell.valueOf(name);*/
			return null;
		}

		public void write(JsonWriter writer, Cell value) throws IOException {
			if (value == null) {
				writer.nullValue();
				return;
			}
			String name = value.getType().code;
			writer.value(name);
		}
	}
	
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
	
	public Cell(Game game, int i, int j, CellType type) {
		this.game = game;
		this.i = i;
		this.j = j;
		this.type = type; //CellType.E;
	}

	public Cell(Game game, int i, int j) {
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

	@Override
	public void rotate(DeltaAngle delta) {
		throw new RuntimeException("Not supported");
	}
}
