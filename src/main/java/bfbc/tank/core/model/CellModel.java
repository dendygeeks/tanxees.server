package bfbc.tank.core.model;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.Cell;
import bfbc.tank.core.api.CellType;

public class CellModel implements Cell {
	
	@Expose
	private CellType type;
	
	public CellModel(CellType type) {
		this.type = type;
	}
	
	public void setType(CellType type) {
		this.type = type;
	}
	
	@Override
	public CellType getType() {
		return type;
	}

}
