package bfbc.tank.core;

public enum CellType {
	EMPTY("E"), 
	CONCRETE("C"), 
	BRICKS("B"), 
	DARK_CONCRETE("DC");
	
	public final String code;
	
	CellType(String code) {
		this.code = code;
	}
	
	/**
	 * @return <code>true</code> if this cell type is impassable unless destroyed 
	 */
	public boolean isWall() {
		return this.equals(CONCRETE) ||
		       this.equals(BRICKS) ||
		       this.equals(DARK_CONCRETE);
	}
}
