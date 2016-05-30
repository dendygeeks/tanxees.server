package dendygeeks.tanxees.server;

import java.util.HashMap;

import com.google.gson.annotations.Expose;

import dendygeeks.tanxees.api.java.interfaces.Appearance;
import dendygeeks.tanxees.api.java.interfaces.CellType;
import dendygeeks.tanxees.api.java.interfaces.UnitType;

public class GameSetup {
	@Expose
	private String[] playerIds;
	@Expose
	private CellType[] map;
	@Expose
	private HashMap<String, SpawnConfig> spawnConfigs;
	@Expose
	private HashMap<String, Appearance> appearances;
	@Expose
	private HashMap<String, UnitType> unitTypes;
	
	public String[] getPlayerIds() {
		return playerIds;
	}
	public CellType[] getMap() {
		return map;
	}
	public HashMap<String, SpawnConfig> getSpawnConfigs() {
		return spawnConfigs;
	}
	public HashMap<String, Appearance> getAppearances() {
		return appearances;
	}
	public HashMap<String, UnitType> getUnitTypes() {
		return unitTypes;
	}
	
	public GameSetup(String[] playerIds, CellType[] map, HashMap<String, SpawnConfig> spawnConfigs, 
	                 HashMap<String, Appearance> appearances, HashMap<String, UnitType> unitTypes) {
		
		this.playerIds = playerIds.clone();
		this.map = map.clone();
		this.spawnConfigs = new HashMap<>(spawnConfigs);
		this.appearances = new HashMap<>(appearances);
		this.unitTypes = new HashMap<>(unitTypes);
	}
}
