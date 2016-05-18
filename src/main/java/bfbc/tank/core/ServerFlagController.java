package bfbc.tank.core;

import bfbc.tank.core.model.FlagModel;

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
