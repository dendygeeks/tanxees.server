package bfbc.tank.core.controllers;

import bfbc.tank.api.model.FlagModel;

public class ServerFlagController extends ServerUnitController {
	private static final double SIZE = 44;
	
	ServerFlagController(double posX, double posY)
	{
		super(new FlagModel(SIZE, SIZE, posX, posY, 0, false));
	}
	
	public FlagModel getFlagModel() {
		return (FlagModel)getUnitModel();
	}
	
	@Override
	public boolean isActive() {
		return !getFlagModel().isCrashed();
	}

}
