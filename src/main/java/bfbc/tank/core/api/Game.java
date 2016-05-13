package bfbc.tank.core.api;

import java.util.HashMap;

public interface Game {
	public double getCellSize();
	public int getFieldWidth();
	public int getFieldHeight();
	public boolean isOver();
	
	public HashMap<String, Player> getPlayers();
	public Flag getFlag();
	public Cell[] getField();
}
