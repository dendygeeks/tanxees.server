package bfbc.tank.core;

import bfbc.tank.core.model.TheStateModel;

public class TheStateController {
	private ServerGameController gameController;
	private TheStateModel theStateModel;

	public TheStateController(ServerGameController gameController, String activePlayerId) {
		this.gameController = gameController;
		this.theStateModel = new TheStateModel(gameController.getGameModel(), activePlayerId);
	}
	
	public ServerGameController getGameController() {
		return gameController;
	}
	
	public synchronized String toJson() {
		return GlobalServices.getGson().toJson(theStateModel);
	}
}