package bfbc.tank.core.model;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.GlobalServices;

public class TheStateModel {
	@Expose
	private final GameModel game;
	@Expose
	private final String activePlayerId;

	public TheStateModel(GameModel game, String activePlayerId) {
		this.game = game;
		this.activePlayerId = activePlayerId;
	}
	
	public synchronized String toJson() {
		return GlobalServices.getGson().toJson(this);
	}
}