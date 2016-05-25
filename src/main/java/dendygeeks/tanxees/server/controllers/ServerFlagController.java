package dendygeeks.tanxees.server.controllers;

import dendygeeks.tanxees.api.java.model.FlagModel;

public class ServerFlagController extends ServerUnitController {
	private static final double SIZE = 44;
	
	ServerFlagController(double posX, double posY)
	{
		super(new FlagModel(SIZE, SIZE, posX, posY, 0, false));
	}
	
	public FlagModel getFlagModel() {
		return (FlagModel)getUnitModel();
	}
	
	public boolean isCrashed() {
		return getFlagModel().isCrashed();
	}
}
