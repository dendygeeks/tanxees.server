package bfbc.tank.core;

public enum CellType {
	E, C, B;
	
	public boolean isOccupied() {
		return this.equals(C) ||
		       this.equals(B);
	}
}
