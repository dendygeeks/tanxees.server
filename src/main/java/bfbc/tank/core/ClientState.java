package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.controllers.ServerDebugDataController;

public class ClientState {
	@Expose
	private final PlayerKeys keys;
	
	@Expose
	private final ServerDebugDataController debugData;

	public ClientState(PlayerKeys keys, ServerDebugDataController debug) {
		this.keys = keys;
		this.debugData = debug;
	}
	
	public PlayerKeys getKeys() {
		return keys;
	}
	
	public ServerDebugDataController getDebugData() {
		return debugData;
	}
	
	public static ClientState fromJson(String json) {
		return GlobalServices.getGson().fromJson(json, ClientState.class);
	}
}
