package bfbc.tank.core;

import com.google.gson.annotations.Expose;

public class ClientState {
	@Expose
	private final PlayerKeys keys;
	
	@Expose
	private final DebugData debugData;

	public ClientState(PlayerKeys keys, DebugData debug) {
		this.keys = keys;
		this.debugData = debug;
	}
	
	public PlayerKeys getKeys() {
		return keys;
	}
	
	public DebugData getDebugData() {
		return debugData;
	}
	
	public static ClientState fromJson(String json) {
		return GlobalServices.getGson().fromJson(json, ClientState.class);
	}
}
