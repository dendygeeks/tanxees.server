package bfbc.tank.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.GlobalServices;
import bfbc.tank.core.api.Cell;
import bfbc.tank.core.api.Flag;
import bfbc.tank.core.api.Game;
import bfbc.tank.core.api.Missile;
import bfbc.tank.core.api.Player;

public class GameModel implements Game {
		
	@Expose
	private final int fieldWidth;// = 28 * 2;
	@Expose
	private final int fieldHeight;// = 26 * 2;
	@Expose
	private final double cellSize;
	
	@Expose
	private boolean isOver = false;
	
	@Expose
	private volatile HashMap<String, Player> players = new HashMap<>();
	@Expose
	private volatile FlagModel flag;
	
	@Expose
	private volatile CellModel[] field;// = new Cell[fieldWidth * fieldHeight];
	
	public GameModel(int fieldWidth, int fieldHeight, boolean isOver, FlagModel flag,
			CellModel[] field, int cellSize) {
		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		this.isOver = isOver;
		this.flag = flag;
		this.field = field;
		this.cellSize = cellSize;
	}

	public synchronized String toJson() {
		return GlobalServices.getGson().toJson(this);
	}

	@Override
	public double getCellSize() {
		return cellSize;
	}

	@Override
	public int getFieldWidth() {
		return fieldWidth;
	}

	@Override
	public int getFieldHeight() {
		return fieldHeight;
	}

	@Override
	public boolean isOver() {
		return isOver;
	}

	@Override
	public Map<String, Player> getPlayers() {
		return players;
	}
	
	@Override
	public Flag getFlag() {
		return flag;
	}

	@Override
	public Cell[] getField() {
		return field;
	}
	
	public String findPlayerId(PlayerModel player) {
		for (Entry<String, Player> e : players.entrySet()) {
			if (e.getValue() == player) return e.getKey();
		}
		return null;
	}

	public String findPlayerIdByTank(PlayerUnitModel tank) {
		for (Entry<String, Player> e : players.entrySet()) {
			if (e.getValue().getUnit() == tank) return e.getKey();
		}
		return null;
	}
	
	public Player findMissileOwner(Missile missile) {
		for (Player p : players.values()) {
			if (p.ownsMissile(missile)) return p;
		}
		return null;
	}

	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}

	/*public void setPlayers(HashMap<String, Player> players) {
		this.players = players;
	}*/

	public void setFlag(FlagModel flag) {
		this.flag = flag;
	}

	public void setField(CellModel[] field) {
		this.field = field;
	}
}
